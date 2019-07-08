/**
 * © Nowina Solutions, 2019
 *
 * Concédée sous licence EUPL, version 1.1 ou – dès leur approbation par la Commission européenne - versions ultérieures de l’EUPL (la «Licence»).
 * Vous ne pouvez utiliser la présente œuvre que conformément à la Licence.
 * Vous pouvez obtenir une copie de la Licence à l’adresse suivante:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Sauf obligation légale ou contractuelle écrite, le logiciel distribué sous la Licence est distribué «en l’état»,
 * SANS GARANTIES OU CONDITIONS QUELLES QU’ELLES SOIENT, expresses ou implicites.
 * Consultez la Licence pour les autorisations et les restrictions linguistiques spécifiques relevant de la Licence.
 */
package lu.nowina.rsign.service;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import lu.nowina.rsign.client.UserInfo;

public class SQLUserInfoRetriever implements UserInfoRetriever {

	private static final Logger logger = LoggerFactory.getLogger(SQLUserInfoRetriever.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${agent.user_info.sql}")
	private String sqlTemplate;

	@Autowired
	public File inboundReadDirectory;

	@Override
	public UserInfo getUserInfo(File file) {

		String sql = evaluateSQLExpression(file);
		logger.debug("Template " + sqlTemplate + " evaluates to " + sql);

		return jdbcTemplate.query(sqlTemplate, new ResultSetExtractor<UserInfo>() {
			@Override
			public UserInfo extractData(ResultSet rs) throws SQLException, DataAccessException {

				if (rs.first()) {

					UserInfo info = new UserInfo();
					info.setFirstName(rs.getString(1));
					info.setLastName(rs.getString(2));
					info.setEmail(rs.getString(3));
					info.setPhoneNumber(rs.getString(4));

					if (rs.next()) {
						logger.warn("More than one result for sql " + sql);
					}

					return info;

				} else {
					throw new IllegalStateException("No matching result");
				}

			}
		});

	}

	public String evaluateSQLExpression(File file) {
		ExpressionParser expressionParser = new SpelExpressionParser();
		Expression expression = expressionParser.parseExpression(sqlTemplate);

		expression.setValue("filename", file.getName());

		List<String> path = getPath(file, inboundReadDirectory);
		logger.debug("Path is " + path);
		for (int i = 0; i < path.size(); i++) {
			expression.setValue("dir" + i, path.get(i));
		}
		for (int i = 0; i < path.size(); i++) {
			expression.setValue("parent" + i, path.get(path.size() - 1 - i));
		}

		return expression.getValue(String.class);
	}

	public List<String> getPath(File file, File baseDir) {

		List<String> path = new ArrayList<>();

		File dir = file.getParentFile();
		while (dir != null && !dir.equals(baseDir)) {
			path.add(dir.getName());
			dir = dir.getParentFile();
		}

		Collections.reverse(path);

		return path;

	}

}
