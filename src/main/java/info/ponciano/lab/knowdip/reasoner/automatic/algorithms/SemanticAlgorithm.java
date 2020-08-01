/*
 * Copyright (C) 2019 Jean-Jacques Ponciano
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package info.ponciano.lab.knowdip.reasoner.automatic.algorithms;

import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.reasoner.automatic.PropVar;
import info.ponciano.lab.knowdip.KD;
import info.ponciano.lab.knowdip.reasoner.automatic.SemObject;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pisemantic.PiRestriction;
import info.ponciano.lab.pisemantic.PiRestrictions;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author Jean-Jacques
 */
class SemanticAlgorithm extends RootAlgorithm {

    private SparqlOutput output;
    private final String input;

    SemanticAlgorithm(OntClass origin, PiOnt piont) throws KnowdipException, PiOntologyException {
        super(origin, piont);
        this.input = "?i0";
        this.initOutput();
    }

    String getSparqlOutput() {
        return this.output.toString();
    }

    private void initOutput() throws KnowdipException, PiOntologyException {
        /**
         * if the algorithms is an algorithm of feature extraction
         */
        if (outputs.size() > 1) {// if the algorithms has more than one outputs restriction
            throw new UnsupportedOperationException("unsuported number of output for " + this.origin);
        } else if (outputs.isEmpty()) {  //test if the algorithms has data type restiction.
            PiRestriction outputres = this.dataOutputs.getFirst();//the algorithm do not provide output but characteristics
            OntProperty property = outputres.getProperty();//get the property corresponding to the restriction

            ExtendedIterator<RDFNode> listSeeAlso = property.listSeeAlso(); //get all properties anotated by "see also"
            if (!listSeeAlso.hasNext()) {//if no property is found.
                throw new KnowdipException("the data ouputs restriction " + property + " has no see also anotation");
            }
            //get the property anotated by "see also" 
            OntProperty seeAlsoPprt = this.piont.getOntProperty(listSeeAlso.next().asResource().getURI());

            this.output = new SparqlOutput((seeAlsoPprt).getRange());
            this.output.characterize(this.input, seeAlsoPprt);

            if (listSeeAlso.hasNext()) {//if another property is found.
                throw new KnowdipException("the data ouputs restriction " + property + " has more than one see also anotation");
            }
        } else {
            //the algorithms return object
            PiRestriction resOut = outputs.getFirst();
            Resource outType = resOut.getResource();//get the type of the restriction object. 
            SemObject semObject = new SemObject(piont.getOntClass(outType), piont);//create new sem object
            this.output = new SparqlOutput(outType); // STEP 1: set the output 's type

            // STEP 2: set output characteristics and value 
            //get all properties of the object
            PiRestrictions characteristics = semObject.getCharacteristics();  //get every restrictions that characterize the output 

            for (PiRestriction characteristic : characteristics.toList()) {
                //get type of the chara
                Resource resource = characteristic.getResource();
                OntProperty property = characteristic.getProperty();

                //link the chara type to input, parameters or prerequisite
                int index = this.firstIndexOf(this.inputs, resource);
                if (index >= 0) {
                    this.output.isCharaByInput("?i" + index, property);
                } else {
                    index = firstIndexOf(this.parameters, resource);
                    if (index >= 0) {
                        this.output.isCharaByPara("?p" + index, property);
                    } else {
                        //the output has share a same characteristics 
                        //with the input
                        throw new KnowdipException(this.origin.getLocalName() + ": Output restriction " + property.getLocalName() + " on " + resource.getLocalName() + "  can not be satisfy!\n Information about the object restriction on " + property.getLocalName() + " is missing!");
                    }
                }
            }
        }
    }

    private int firstIndexOf(PiRestrictions res, Resource resource) throws PiOntologyException {
        List<PiRestriction> list = res.toList();
        for (int i = 0; i < list.size(); i++) {
            Resource resData = list.get(i).getResource();
            if (resData.equals(resource)) {
                return i;
            } else {
                List<OntClass> subClass = this.piont.getSubClass(this.piont.getOntClass(resource), false);
                for (OntClass subClas : subClass) {
                    if (resData.getURI().equals(subClas.getURI())) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    protected String sparqlSelectInputs() {
        String query = "";
        //for each input
        for (int i = 0; i < this.inputs.size(); i++) {
            PiRestriction inputres = this.inputs.get(i);
            //get property 
            // OntProperty property = input.getProperty();
            Resource type = inputres.getResource();
            String var = "?i" + i;
            //select input according to its type
            query += var + " <" + RDF.type + "> <" + type + "> .\n ";
             query += "FILTER NOT EXISTS {"+var + " <" + RDF.type + "> <" + KD.NOTHING + "> . } \n ";
        }
        String init = "FILTER NOT EXISTS {";
        //that it is not yet processed
        String nyp = init;
        List<PropVar> lwi = this.output.linkWithInputs;

        for (int j = 0; j < lwi.size(); j++) {
            PropVar pv = lwi.get(j);

            if (pv != null) {
                nyp += " ?something  <" + pv.getOntProperty() + "> " + pv.getVariable();

                if (j < lwi.size() - 1) {
                    nyp += " . ";
                }
                //  query += "FILTER NOT EXISTS {" +  pv.getVariable() + " <" + pv.getOntProperty() + "> ?something } .\n ";
            }
        }
        //if the algorithm is a feature extraction algorithm add look if the input has the right property.
        if (nyp.equals(init)) {
            PropVar ci = this.output.getCharacterizeInput();
            nyp += ci.getVariable() + "<" + ci.getOntProperty() + ">" + " ?x \n";//TODO see when algorithms is not a getter. 
        }
        query += nyp + " } . \n";
        return query;
    }

    /**
     * Get the sparql where content aiming to retrieve parameters from relevant
     * object or data.
     * <img src="http://lab.ponciano.info/public/2019-automatic_detection/selectPara.png"/>
     *
     * @return SPARQL "where" query content.
     */
    protected String sparqlSelectParameters() {
        String query = "";
        //for each parameters
        for (int i = 0; i < this.parameters.size(); i++) {
            PiRestriction para = this.parameters.get(i);
            String var = "?p" + i;

            query += var + " <" + RDF.type + "> <" + para.getResource() + "> .\n ";

            //search the resouces in elements that the algorithms is relevant to
            for (PiRestriction relevant2 : this.relevantTo.toList()) {
                query += "?entity <" + RDF.type + "> <" + relevant2.getResource() + "> .\n ";
                query += "?entity <" + para.getProperty() + "> " + var + " .\n ";
            }
        }
        return query;
    }

    /**
     * Get every input and parameter of the algorithms to build the Sparql query
     * allowing the algorithms execution by the AEE. The parameter and input are
     * link to the algorithms execution thanks to there respective property
     * defined in the algorithm class restrictions.
     * <img src="http://lab.ponciano.info/public/2019-automatic_detection/algorithm_execution.png">
     * </img>
     *
     * @return Sparl query allowing the algorithm execution.
     */
    protected String[] sparqlAlgoExecution() {
        List<PropVar> varIP = new ArrayList<>();

        //foreach input restrictions
        for (int i = 0; i < this.inputs.size(); i++) {
            PiRestriction inputres = this.inputs.get(i);
            String vari = "?i" + i;
            fillPropertyVar(inputres, varIP, vari);
        }
        //for each parameter restrictions
        for (int i = 0; i < this.parameters.size(); i++) {
            PiRestriction para = this.parameters.get(i);
            String vari = "?p" + i;
            fillPropertyVar(para, varIP, vari);
        }
        String queryForEx = "";
        //get the value corresponding to the input and parameter if 
        //the its is an object
        for (int i = 0; i < varIP.size(); i++) {
            PropVar vi = varIP.get(i);
            OntProperty pprt = vi.getOntProperty();
            //if it is a value
            if (pprt.isDatatypeProperty()) {
                String v = "?v" + i;
                queryForEx += vi.getVariable() + " <" + pprt + "> " + v
                        + " . ";
                vi.setVariable(v);
            }
        }
        String queryEx = "\n ?out <" + this.origin.getURI() + "> ( ";
        for (int i = 0; i < varIP.size(); i++) {
            //get the object         
            queryEx += "\"" + varIP.get(i).getOntProperty() + " =\" " + varIP
                    .get(i).getVariable() + " ";

        }
        queryEx += ")  . \n  ";

        return new String[]{queryForEx, queryEx};

    }

    private void fillPropertyVar(PiRestriction input, List<PropVar> varIP, String var) {
        List<OntProperty> valueProperties = getValueProperties(input);
        //if it is a full object as a point cloud without data property value
        if (valueProperties == null || valueProperties.isEmpty()) {
            //add the property
            varIP.add(new PropVar(input.getProperty(), var));
        } else {//add the data property for each values
            for (int j = 0; j < valueProperties.size(); j++) {
                varIP.add(new PropVar(valueProperties.get(j), var));
            }
        }
    }

    /**
     * Get the list of property use to link values to the individual
     *
     * @param input individual to be analysed
     * @return the list of property that link the values to the indivual or null
     * if the individual has no unique value.
     */
    private List<OntProperty> getValueProperties(PiRestriction input) {
        List<OntProperty> properties
                = new ArrayList<>();
        SemObject semObject
                = new SemObject(
                        this.piont
                                .getOntClass(input
                                        .getResource()), piont
                );
        PiRestrictions values
                = semObject
                        .getValues();

        if (values
                != null && !values
                        .isEmpty()) {
            values
                    .toList().forEach((value) -> {
                        properties
                                .add(value
                                        .getProperty());

                    });

        }
        return properties;

    }

    /**
     * Get sparql query to construct and execute algorithms.
     * <p>
     * Example:
     * <pre>
     * CONSTRUCT  {
     * ?out <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://lab.ponciano.info/knowdip#FullPointCloud> .
     * ?out <http://lab.ponciano.info/knowdip#readFrom> ?i0 .
     * }
     * WHERE{
     * ?i0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://lab.ponciano.info/knowdip#File> .
     * minus { ?something  <http://lab.ponciano.info/knowdip#readFrom> ?i0 } .
     * ?i0 <http://lab.ponciano.info/knowdip#hasSource> ?v0  .
     * (?out)<http://lab.ponciano.info/knowdip#LoadCloud>( "http://lab.ponciano.info/knowdip#hasSource=" ?v0 ) . }
     * </pre>
     * </p>
     *
     * @return sparql query
     *
     */
    private String getSparqlWhere() {
        String query
                = " \n GRAPH <" + KD.URI
                + "> {\n "
                + "WHERE{\n";
        query
                += sparqlSelectInputs();
        //Satisfy every parameter
        query
                += sparqlSelectParameters();
        query
                += sparqlAlgoExecution();
        query
                += " } }";

        return query;
    }

}
