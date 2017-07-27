package OntologyConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class jdbcmysql {
	public Connection con = null; // Database objects
	public Statement stat = null; // ����,�ǤJ��sql������r��
	public ResultSet rs = null; // ���G��
	// ����,�ǤJ��sql���w�x���r��,�ݭn�ǤJ�ܼƤ���m �C���Q��?�Ӱ��Х�
	private PreparedStatement pst = null;
	
	public jdbcmysql() {
		try {
			// ���Udriver
			Class.forName("com.mysql.jdbc.Driver");
			// ���oconnection
			con = DriverManager.getConnection("jdbc:mysql://localhost/tw_patent", "root", "titan");
			System.out.println("MySQL Connection");
		} catch (ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		} catch (SQLException x) {
			System.out.println("Exception :" + x.toString());
		}
	}
	
	// �d�߸��
	// �i�H�ݬݦ^�ǵ��G���Ψ��o��Ƥ覡
	public void SelectTable() {
		String selectSQL = "select * from crawler";
		try {
			stat = con.createStatement();
			rs = stat.executeQuery(selectSQL);
			int count = 0;
			while (rs.next()) {
				System.out.println(rs.getString("id") + "\t" + rs.getString("name"));
				count++;
				if (count == 3) break;
			}
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	// ����ϥΧ���Ʈw��,�O�o�n�����Ҧ�Object
	// �_�h�b����Timeout��,�i��|��Connection poor�����p
	public void Close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stat != null) {
				stat.close();
				stat = null;
			}
			if (pst != null) {
				pst.close();
				pst = null;
			}
			con.close();
		} catch (SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}
}
