package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelWriter;

public class OntologyConstruction {
	jdbcmysql mysql = new jdbcmysql();
	public void getOntology() {
		try {
			OWLModel owlModel = ProtegeOWL.createJenaOWLModel();
			ContentAnalysis(owlModel);
			
			FileOutputStream fileOutputStream = new FileOutputStream(new File("patent_ontology.owl"));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
			OWLModelWriter owlModelWriter = new OWLModelWriter(owlModel, owlModel.getTripleStoreModel().getActiveTripleStore(), outputStreamWriter);
			owlModelWriter.write();
			fileOutputStream.flush();
			outputStreamWriter.flush();
			fileOutputStream.close();
			outputStreamWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void ContentAnalysis(OWLModel owlModel) throws Exception {
		// �إ� "�M�Q��T" Class
		OWLNamedClass patent_information_OWLClass = owlModel.createOWLNamedClass("�M�Q��T");
		// �b "�M�Q����" class ���s�W��� child class "��ڤ�����_IPC"�B"�]�p������_LOC"
		OWLNamedClass patent_category_OWLClass = owlModel.createOWLNamedClass("�M�Q����");
		OWLNamedClass IPC_OWLClass = owlModel.createOWLNamedSubclass("��ڤ�����_IPC", patent_category_OWLClass);	
		OWLNamedClass LOC_OWLClass = owlModel.createOWLNamedSubclass("�]�p������_LOC", patent_category_OWLClass);
		// �b "�M�Q���Y�H" class ���s�W��� child class "�o���H"�B"�ӽФH"
		OWLNamedClass patent_relationship_person_OWLClass = owlModel.createOWLNamedClass("�M�Q���Y�H");
		OWLNamedClass inventor_OWLClass = owlModel.createOWLNamedSubclass("�o���H", patent_relationship_person_OWLClass);
		OWLNamedClass applicant_OWLClass = owlModel.createOWLNamedSubclass("�ӽФH", patent_relationship_person_OWLClass);
		
		System.out.println("Create Ontology Successfully");
	}
}
