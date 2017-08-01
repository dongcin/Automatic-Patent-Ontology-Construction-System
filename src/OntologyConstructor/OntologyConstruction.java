package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
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

import jdbc.DBOperations;
import jdbc.Mysql_Select;

public class OntologyConstruction {
	DBOperations dbOperations = new DBOperations();
	
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
	final String regex_reference_patentID = "(\\W?)([A-Z]{2,4}[0-9]{1,5}[A-Z-\\/��]?[0-9]{2,}[-(]?[A-Z]?[0-9]+[)]?[A-Z]?[A-Z]?)";
	Pattern pattern;
	Matcher matcher;
	
	final String selectSQL = "select * from crawler";
	
	public void CreateOntology() {
		try {
			owlModel = ProtegeOWL.createJenaOWLModel();
			CreateOWLClass();
			CreateOWLDataProperty();
			CreateOWLObjectProperty();
			
			ContentAnalysis();
			
			SaveOntology_to_OWL();
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
	private void PatentID_OWLIndividual_AddDataPropertyValue() throws Exception {
		patentID_OWLIndividual.addPropertyValue(patent_name_OWLDataProperty, patent_name);
		patentID_OWLIndividual.addPropertyValue(application_date_OWLDataProperty, patent_applicationDate);
		patentID_OWLIndividual.addPropertyValue(inventor_OWLDataProperty, patent_inventor);
		patentID_OWLIndividual.addPropertyValue(applicant_OWLDataProperty, patent_applicant);
	}
	
	/**
	 * �s�W �Q�ѦҪ� "�M�Q�s��" ��� �P �ѦҸ�  "�M�Q�s��" ��Ҥ����� "is_referenced_by(�Q�Ѧ�)" �����ݩ����p
	 */
	private void PatentID_is_referenced_by_OWLIndividual_AddObjectPropertyValue() throws Exception {
		patentID_is_referenced_by_OWLIndividual.addPropertyValue(is_referenced_by_OWLObjectProperty, patentID_OWLIndividual);
	}
	
	/**
	 * �p�G�w���W�� OWLIndividual_name ����ҡA�N�������o OWLIndividual�A�Ϥ��A�b OWLClass ���O���إ߷s�� OWLIndividual
	 */
	private OWLIndividual getOWLIndividual(OWLNamedClass OWLClass, String OWLIndividual_name) throws Exception {
		OWLIndividual _OWLIndividual = owlModel.getOWLIndividual(OWLIndividual_name);
		if (_OWLIndividual == null) return OWLClass.createOWLIndividual(OWLIndividual_name);
		else return _OWLIndividual;
	}
	
	private void BuildRelationships_PatentsAreReferencedByPatents() throws Exception {
//		String[] patent_reference_ary = patent_references.split("; ");
		// ���W��F���G��X �ѦҤ��m�� �Ҧ��� �M�Q�s�� (�����a�B��쪺�M�Q�s��)
		matcher = SetMatcher(regex_reference_patentID, patent_references);
		// ���X�ŦX������
		while (matcher.find()) {
		    if (IsPatentID_Regex()) {
		    	String patent_reference_patentID = matcher.group(2);
		    	// �p�G�w���W�� patent_reference_patentID ����ҡA�N�������o "�Q�ѦҪ��M�Q�s��" ���
		    	// �Ϥ��A�b "�M�Q�s��" ���O���إ߷s�� "�Q�ѦҪ��M�Q�s��" ���
		    	patentID_is_referenced_by_OWLIndividual = getOWLIndividual(patent_id_OWLClass, patent_reference_patentID);
		    	// �s�W �Q�ѦҪ� "�M�Q�s��" ��� �P �ѦҸ�  "�M�Q�s��" ��Ҥ����� "is_referenced_by(�Q�Ѧ�)" �����ݩ����p
		    	PatentID_is_referenced_by_OWLIndividual_AddObjectPropertyValue();
	    	}
		}
	}
	
	private Matcher SetMatcher(String _regex, String matcher_value) throws Exception {
		pattern = Pattern.compile(_regex);
		return pattern.matcher(matcher_value);
	}
	
	private boolean IsPatentID_Regex() {
		// TODO Array PatentID Filter
		return !matcher.group(1).contains(".");
	}
	
	private boolean hasPatentReferences() {
		// TODO ��L�L�ѦҤ��m���i��
		return patent_references != "NULL";
	}
	
	private void ContentAnalysis() throws Exception {
		dbOperations.SelectTable(selectSQL, new Mysql_Select() {
			@Override
			public void select(ResultSet rs) throws SQLException {
				int count = 0;
				while (rs.next()) {
					// DB �����O�x�W�M�Q�A�ҥH�۰ʥ[�W��X
					patent_id = "TW" + rs.getString("id");
					patent_name = rs.getString("name");
					patent_inventor = rs.getString("inventor");
					patent_applicant = rs.getString("applicant");
					patent_references = rs.getString("reference");
					patent_applicationDate = rs.getString("application_date");
					//System.out.println(patent_id + "\t" + patent_name);
					
					try {
						// �p�G�w���W�� patent_id ����ҡA�N�������o "�M�Q�s��" ��ҡA�Ϥ��A�b "�M�Q�s��" ���O���إ߷s�� "�M�Q�s��" ���
						patentID_OWLIndividual = getOWLIndividual(patent_id_OWLClass, patent_id);
						// �b "�M�Q�s��" ����Ҥ��]�w "����ݩ�"�G�M�Q�W�١B�ӽФ�B�o���H�B�ӽФH
						PatentID_OWLIndividual_AddDataPropertyValue();
						// �p�G �ӵ��M�Q�S�� "�ѦҤ��m"�A�N���|�إ� "is_referenced_by(�Q�Ѧ�)" �����ݩ� ���p
						if (hasPatentReferences()) BuildRelationships_PatentsAreReferencedByPatents();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					count++;
					if (count == 1000) break;
				}
			}
		});
	}
	
	/**
	 * �N Ontology �s�� OWL ��
	 */
	private void SaveOntology_to_OWL() throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(new File("patent_ontology.owl"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
		OWLModelWriter owlModelWriter = new OWLModelWriter(owlModel, 
				owlModel.getTripleStoreModel().getActiveTripleStore(), outputStreamWriter);
		owlModelWriter.write();
		fileOutputStream.flush();
		outputStreamWriter.flush();
		fileOutputStream.close();
		outputStreamWriter.close();
		System.out.println("Create Ontology Successfully");
	}
}
