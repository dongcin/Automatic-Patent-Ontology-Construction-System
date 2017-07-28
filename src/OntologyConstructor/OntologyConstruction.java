package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelWriter;

public class OntologyConstruction {
	jdbcmysql mysql = new jdbcmysql();
	
	OWLModel owlModel;
	
	// Class (���O)
	OWLNamedClass patent_information_OWLClass;	// �M�Q��T
	OWLNamedClass patent_id_OWLClass;			// �M�Q�s��
	OWLNamedClass patent_category_OWLClass;		// �M�Q����
	OWLNamedClass IPC_OWLClass;					// ��ڤ�����_IPC
	OWLNamedClass LOC_OWLClass;					// �]�p������_LOC
//	OWLNamedClass patent_relationship_person_OWLClass;	// �M�Q���Y�H
//	OWLNamedClass inventor_OWLClass;			// �o���H
//	OWLNamedClass applicant_OWLClass;			// �ӽФH
	
	// DataProperty (����ݩ�)
	OWLDatatypeProperty patent_name_OWLDataProperty;		// �M�Q�W��
	OWLDatatypeProperty application_date_OWLDataProperty;	// �ӽФ�
	OWLDatatypeProperty reference_OWLDataProperty;			// �ѦҤ��m
	OWLDatatypeProperty inventor_OWLDataProperty;			// �o���H
	OWLDatatypeProperty applicant_OWLDataProperty;			// �ӽФH
	
	// ObjectProperty (�����ݩ�)
	OWLObjectProperty is_referenced_by_OWLObjectProperty;	// is_referenced_by (�Q�Ѧ�)
	
	// Individual (���)
	OWLIndividual patentID_OWLIndividual;		// �M�Q�s��
	OWLIndividual patentID_is_referenced_by_OWLIndividual;	// �M�Q�s�� (�Q�Ѧ�)
	
	String patent_id;
	String patent_name;
	String patent_applicationDate;
	String patent_inventor;
	String patent_applicant;
	String patent_references;
	
	// ���W��F���G��X �ѦҤ��m�� �Ҧ��� �M�Q�s�� (�����a�B��쪺�M�Q�s��)
	final String regex = "(\\W?)([A-Z]{2,4}[0-9]{1,5}[A-Z-\\/��]?[0-9]{2,}[-(]?[A-Z]?[0-9]+[)]?[A-Z]?[A-Z]?);?";
	final Pattern pattern = Pattern.compile(regex);
	Matcher matcher;
	
	public void CreateOntology() {
		try {
			owlModel = ProtegeOWL.createJenaOWLModel();
			CreateOWLClass();
			CreateOWLDataProperty();
			CreateOWLObjectProperty();
			
			ContentAnalysis();
			
			SaveOntology();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Class:
	 * - �M�Q��T
	 * 	 - �M�Q�s��
	 * - �M�Q����
	 * 	 - ��ڤ�����_IPC
	 * 	 - �]�p������_LOC
	 */
	private void CreateOWLClass() throws Exception {
		// �إ� "�M�Q��T" Class ���s�W�@�� child class "�M�Q�s��"
		patent_information_OWLClass = owlModel.createOWLNamedClass("�M�Q��T");
		patent_id_OWLClass = owlModel.createOWLNamedSubclass("�M�Q�s��", patent_information_OWLClass);	
		// �b "�M�Q����" class ���s�W��� child class "��ڤ�����_IPC"�B"�]�p������_LOC"
		patent_category_OWLClass = owlModel.createOWLNamedClass("�M�Q����");
		IPC_OWLClass = owlModel.createOWLNamedSubclass("��ڤ�����_IPC", patent_category_OWLClass);	
		LOC_OWLClass = owlModel.createOWLNamedSubclass("�]�p������_LOC", patent_category_OWLClass);
		// �b "�M�Q���Y�H" class ���s�W��� child class "�o���H"�B"�ӽФH"
//		patent_relationship_person_OWLClass = owlModel.createOWLNamedClass("�M�Q���Y�H");
//		inventor_OWLClass = owlModel.createOWLNamedSubclass("�o���H", patent_relationship_person_OWLClass);
//		applicant_OWLClass = owlModel.createOWLNamedSubclass("�ӽФH", patent_relationship_person_OWLClass);
	}
	
	/**
	 * DataProperty:
	 * - �M�Q�W��
	 * - �ӽФ�
	 * - �ѦҤ��m
	 * - �o���H
	 * - �ӽФH
	 */
	private void CreateOWLDataProperty() throws Exception {
		patent_name_OWLDataProperty = owlModel.createOWLDatatypeProperty("�M�Q�W��");
		application_date_OWLDataProperty = owlModel.createOWLDatatypeProperty("�ӽФ�");
		reference_OWLDataProperty = owlModel.createOWLDatatypeProperty("�ѦҤ��m");
		inventor_OWLDataProperty = owlModel.createOWLDatatypeProperty("�o���H");
		applicant_OWLDataProperty = owlModel.createOWLDatatypeProperty("�ӽФH");
	}
	
	/**
	 * ObjectProperty:
	 * - is_referenced_by(�Q�Ѧ�)�G"�M�Q�s��" is_referenced_by "�M�Q�s��"
	 */
	private void CreateOWLObjectProperty() throws Exception {
		// �إ� "is_referenced_by(�Q�Ѧ�)" �����ݩʡADomain �M Range ���]�� "�M�Q�s��"
		// "�M�Q�s��" is_referenced_by "�M�Q�s��"
		is_referenced_by_OWLObjectProperty = owlModel.createOWLObjectProperty("is_referenced_by(�Q�Ѧ�)");
		is_referenced_by_OWLObjectProperty.setDomain(patent_id_OWLClass);
		is_referenced_by_OWLObjectProperty.setRange(patent_id_OWLClass);
	}
	
	/**
	 * �b "�M�Q�s��" ����Ҥ��]�w "����ݩ�"�G�M�Q�W�١B�ӽФ�B�o���H�B�ӽФH
	 */
	private void PatentID_OWLIndividual_SetDataPropertyValue() throws Exception {
		patentID_OWLIndividual.setPropertyValue(patent_name_OWLDataProperty, patent_name);
		patentID_OWLIndividual.setPropertyValue(application_date_OWLDataProperty, patent_applicationDate);
		patentID_OWLIndividual.setPropertyValue(inventor_OWLDataProperty, patent_inventor);
		patentID_OWLIndividual.setPropertyValue(applicant_OWLDataProperty, patent_applicant);
	}
	
	private void ContentAnalysis() throws Exception {
		String selectSQL = "select * from crawler";
		try {
			mysql.stat = mysql.con.createStatement();
			mysql.rs = mysql.stat.executeQuery(selectSQL);
			int count = 0;
			while (mysql.rs.next()) {
				patent_id = "TW" + mysql.rs.getString("id");
				patent_name = mysql.rs.getString("name");
				patent_applicationDate  = mysql.rs.getString("application_date");
				patent_inventor  = mysql.rs.getString("inventor");
				patent_applicant  = mysql.rs.getString("applicant");
				patent_references = mysql.rs.getString("reference");
//				System.out.println(patent_id + "\t" + patent_name);
				
				// ���o�� "�M�Q�s��" ����ҡA�p�G�S���N�b "�M�Q�s��" ���O���إ߷s�� "�M�Q�s��" ���
				patentID_OWLIndividual = owlModel.getOWLIndividual(patent_id);
				if (patentID_OWLIndividual == null)
					patentID_OWLIndividual = patent_id_OWLClass.createOWLIndividual(patent_id);
				
				// �b "�M�Q�s��" ����Ҥ��]�w "����ݩ�"�G�M�Q�W�١B�ӽФ�B�o���H�B�ӽФH
				PatentID_OWLIndividual_SetDataPropertyValue();
				
				count++;
				if (count == 1000) break;
			}
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			mysql.Close();
		}
	}
	
	/**
	 * �N Ontology �s�� OWL ��
	 */
	private void SaveOntology() throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(new File("patent_ontology.owl"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
		OWLModelWriter owlModelWriter = 
				new OWLModelWriter(owlModel, owlModel.getTripleStoreModel().getActiveTripleStore(), outputStreamWriter);
		owlModelWriter.write();
		fileOutputStream.flush();
		outputStreamWriter.flush();
		fileOutputStream.close();
		outputStreamWriter.close();
		System.out.println("Create Ontology Successfully");
	}
}
