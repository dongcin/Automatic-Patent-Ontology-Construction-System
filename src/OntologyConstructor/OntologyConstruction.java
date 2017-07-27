package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelWriter;

public class OntologyConstruction {
	jdbcmysql mysql = new jdbcmysql();
	
	OWLModel owlModel;
	
	OWLNamedClass patent_information_OWLClass;	// Class�G�M�Q��T
	OWLNamedClass patent_category_OWLClass;		// Class�G�M�Q����
	OWLNamedClass IPC_OWLClass;					// Class�G��ڤ�����_IPC
	OWLNamedClass LOC_OWLClass;					// Class�G�]�p������_LOC
//	OWLNamedClass patent_relationship_person_OWLClass;	// Class�G�M�Q���Y�H
//	OWLNamedClass inventor_OWLClass;			// Class�G�o���H
//	OWLNamedClass applicant_OWLClass;			// Class�G�ӽФH
	
	OWLDatatypeProperty patent_name_OWLDataProperty;
	OWLDatatypeProperty application_date_OWLDataProperty;
	OWLDatatypeProperty reference_OWLDataProperty;
	OWLDatatypeProperty inventor_OWLDataProperty;
	OWLDatatypeProperty applicant_OWLDataProperty;
	
	public void CreateOntology() {
		try {
			owlModel = ProtegeOWL.createJenaOWLModel();
			CreateOWLClass(owlModel);
			CreateOWLDataProperty(owlModel);
			
			ContentAnalysis(owlModel);
			
			SaveOntology();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void CreateOWLClass(OWLModel owlModel) throws Exception {
		// �إ� "�M�Q��T" Class
		patent_information_OWLClass = owlModel.createOWLNamedClass("�M�Q��T");
		// �b "�M�Q����" class ���s�W��� child class "��ڤ�����_IPC"�B"�]�p������_LOC"
		patent_category_OWLClass = owlModel.createOWLNamedClass("�M�Q����");
		IPC_OWLClass = owlModel.createOWLNamedSubclass("��ڤ�����_IPC", patent_category_OWLClass);	
		LOC_OWLClass = owlModel.createOWLNamedSubclass("�]�p������_LOC", patent_category_OWLClass);
		// �b "�M�Q���Y�H" class ���s�W��� child class "�o���H"�B"�ӽФH"
//		patent_relationship_person_OWLClass = owlModel.createOWLNamedClass("�M�Q���Y�H");
//		inventor_OWLClass = owlModel.createOWLNamedSubclass("�o���H", patent_relationship_person_OWLClass);
//		applicant_OWLClass = owlModel.createOWLNamedSubclass("�ӽФH", patent_relationship_person_OWLClass);
	}
	
	private void CreateOWLDataProperty(OWLModel owlModel) throws Exception {
		patent_name_OWLDataProperty = owlModel.createOWLDatatypeProperty("�M�Q�W��");
		application_date_OWLDataProperty = owlModel.createOWLDatatypeProperty("�ӽФ�");
		reference_OWLDataProperty = owlModel.createOWLDatatypeProperty("�ѦҤ��m");
		inventor_OWLDataProperty = owlModel.createOWLDatatypeProperty("�o���H");
		applicant_OWLDataProperty = owlModel.createOWLDatatypeProperty("�ӽФH");
	}
	
	private void CreateOWLIndividual(OWLModel owlModel) throws Exception {
		
	}
	
	
	private void ContentAnalysis(OWLModel owlModel) throws Exception {
		
	}
	
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
