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
	OWLNamedClass patentInformation_OWLClass;	// �M�Q��T
	OWLNamedClass patentID_OWLClass;			// �M�Q�s��
	OWLNamedClass patentCategory_OWLClass;		// �M�Q����
	OWLNamedClass IPC_OWLClass;					// ��ڤ�����_IPC
	OWLNamedClass LOC_OWLClass;					// �]�p������_LOC
//	OWLNamedClass patent_relationship_person_OWLClass;	// �M�Q���Y�H
//	OWLNamedClass inventor_OWLClass;			// �o���H
//	OWLNamedClass applicant_OWLClass;			// �ӽФH
	
	// DataProperty (����ݩ�)
	OWLDatatypeProperty patentName_OWLDataProperty;		// �M�Q�W��
	OWLDatatypeProperty applicationDate_OWLDataProperty;	// �ӽФ�
	OWLDatatypeProperty reference_OWLDataProperty;			// �ѦҤ��m
	OWLDatatypeProperty inventor_OWLDataProperty;			// �o���H
	OWLDatatypeProperty applicant_OWLDataProperty;			// �ӽФH
	
	// ObjectProperty (�����ݩ�)
	OWLObjectProperty isReferencedBy_OWLObjectProperty;	// is_referenced_by (�Q�Ѧ�)
	
	// Individual (���)
	OWLIndividual patentID_OWLIndividual;		// �M�Q�s��
	OWLIndividual patentID_IsReferencedBy_OWLIndividual;	// �M�Q�s�� (�Q�Ѧ�)
	
	String patentID;
	String patentName;
	String patentApplicationDate;
	String patentInventor;
	String patentApplicant;
	String patentReferences;
	
	// ���W��F���G��X �ѦҤ��m�� �Ҧ��� �M�Q�s�� (�����a�B��쪺�M�Q�s��)
	final String regex_Reference_patentID = "(\\W?)([A-Z]{2,4}[0-9]{1,5}[A-Z-\\/��]?[0-9]{2,}[-(]?[A-Z]?[0-9]+[)]?[A-Z]?[A-Z]?)";
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
		patentInformation_OWLClass = owlModel.createOWLNamedClass("�M�Q��T");
		patentID_OWLClass = owlModel.createOWLNamedSubclass("�M�Q�s��", patentInformation_OWLClass);	
		// �b "�M�Q����" class ���s�W��� child class "��ڤ�����_IPC"�B"�]�p������_LOC"
		patentCategory_OWLClass = owlModel.createOWLNamedClass("�M�Q����");
		IPC_OWLClass = owlModel.createOWLNamedSubclass("��ڤ�����_IPC", patentCategory_OWLClass);	
		LOC_OWLClass = owlModel.createOWLNamedSubclass("�]�p������_LOC", patentCategory_OWLClass);
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
		patentName_OWLDataProperty = owlModel.createOWLDatatypeProperty("�M�Q�W��");
		applicationDate_OWLDataProperty = owlModel.createOWLDatatypeProperty("�ӽФ�");
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
		isReferencedBy_OWLObjectProperty = owlModel.createOWLObjectProperty("is_referenced_by(�Q�Ѧ�)");
		isReferencedBy_OWLObjectProperty.setDomain(patentID_OWLClass);
		isReferencedBy_OWLObjectProperty.setRange(patentID_OWLClass);
	}
	
	/**
	 * �b "�M�Q�s��" ����Ҥ��]�w "����ݩ�"�G�M�Q�W�١B�ӽФ�B�o���H�B�ӽФH
	 */
	private void PatentID_OWLIndividual_AddDataPropertyValue() throws Exception {
		patentID_OWLIndividual.addPropertyValue(patentName_OWLDataProperty, patentName);
		patentID_OWLIndividual.addPropertyValue(applicationDate_OWLDataProperty, patentApplicationDate);
		patentID_OWLIndividual.addPropertyValue(inventor_OWLDataProperty, patentInventor);
		patentID_OWLIndividual.addPropertyValue(applicant_OWLDataProperty, patentApplicant);
	}
	
	/**
	 * �s�W �Q�ѦҪ� "�M�Q�s��" ��� �P �ѦҸ�  "�M�Q�s��" ��Ҥ����� "is_referenced_by(�Q�Ѧ�)" �����ݩ����p
	 */
	private void PatentID_IsReferencedBy_PatentID_OWLIndividual_AddObjectPropertyValue() throws Exception {
		patentID_IsReferencedBy_OWLIndividual.addPropertyValue(isReferencedBy_OWLObjectProperty, patentID_OWLIndividual);
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
		//String[] patent_reference_ary = patent_references.split("; ");
		// ���W��F���G��X �ѦҤ��m�� �Ҧ��� �M�Q�s�� (�����a�B��쪺�M�Q�s��)
		matcher = SetMatcher(regex_Reference_patentID, patentReferences);
		// ���X�ŦX������
		while (matcher.find()) {
		    if (IsPatentID_Regex()) {
		    	String patent_reference_patentID = matcher.group(2);
		    	// �p�G�w���W�� patent_reference_patentID ����ҡA�N�������o "�Q�ѦҪ��M�Q�s��" ���
		    	// �Ϥ��A�b "�M�Q�s��" ���O���إ߷s�� "�Q�ѦҪ��M�Q�s��" ���
		    	patentID_IsReferencedBy_OWLIndividual = getOWLIndividual(patentID_OWLClass, patent_reference_patentID);
		    	// �s�W �Q�ѦҪ� "�M�Q�s��" ��� �P �ѦҸ�  "�M�Q�s��" ��Ҥ����� "is_referenced_by(�Q�Ѧ�)" �����ݩ����p
		    	PatentID_IsReferencedBy_PatentID_OWLIndividual_AddObjectPropertyValue();
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
		return patentReferences != null;
	}
	
	private void ContentAnalysis() throws Exception {
		dbOperations.SelectTable(selectSQL, new Mysql_Select() {
			@Override
			public void select(ResultSet rs) throws SQLException {
				int count = 0;
				while (rs.next()) {
					// DB �����O�x�W�M�Q�A�ҥH�۰ʥ[�W��X
					patentID = "TW" + rs.getString("id");
					patentName = rs.getString("name");
					patentInventor = rs.getString("inventor");
					patentApplicant = rs.getString("applicant");
					patentReferences = rs.getString("reference");
					patentApplicationDate = rs.getString("application_date");
					//System.out.println(patent_id + "\t" + patent_name);
					
					try {
						// �p�G�w���W�� patent_id ����ҡA�N�������o "�M�Q�s��" ��ҡA�Ϥ��A�b "�M�Q�s��" ���O���إ߷s�� "�M�Q�s��" ���
						patentID_OWLIndividual = getOWLIndividual(patentID_OWLClass, patentID);
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
