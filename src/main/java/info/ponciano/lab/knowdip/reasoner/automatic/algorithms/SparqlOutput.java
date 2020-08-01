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

import info.ponciano.lab.knowdip.reasoner.automatic.PropVar;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

/**
 * Get the query which represente the content to be insert in the KD by a SPARQL
 * query to integrate the output result of the algorithms in the KB according to
 * its restrictions.
 * <img src="http://lab.ponciano.info/public/2019-automatic_detection/outputIntegration.png"/>
 *
 * @author Jean-Jacques ponciano
 */
class SparqlOutput {

    protected String name;
    protected List<PropVar> linkWithInputs;
    protected List<PropVar> linkWithParameters;
    protected PropVar characterizeInput;
    protected Resource type;

    SparqlOutput(Resource outType) {
        this.type = outType;
        this.name = "?out";
        this.linkWithInputs = new ArrayList<>();
        this.linkWithParameters = new ArrayList<>();
        this.characterizeInput = null;
    }

    /**
     * input property out
     *
     * @param input
     * @param property
     */
    void characterize(String input, OntProperty property) {
        this.characterizeInput = new PropVar(property, input);
    }

    /**
     * out property input
     *
     * @param input
     * @param property
     */
    void isCharaByInput(String input, OntProperty property) {
        this.linkWithInputs.add(new PropVar(property, input));
    }

    /**
     * out property para
     *
     * @param para
     * @param property
     */
    void isCharaByPara(String para, OntProperty property) {
        this.linkWithParameters.add(new PropVar(property, para));
    }

    @Override
    public String toString() {

        String sparql;
        if (characterizeInput != null) {
            sparql = this.characterizeInput.getVariable() + " <" + this.characterizeInput.getOntProperty().getURI() + "> " + this.name + " .\n";
        } else {
            sparql = name + " <" + RDF.type + "> <" + type + "> .\n";

            for (PropVar propVar : this.linkWithParameters) {
                sparql += this.name + " <" + propVar.getOntProperty().getURI() + "> " + propVar.getVariable() + ". \n  ";
            }
            for (PropVar propVar : this.linkWithInputs) {
                sparql += this.name + " <" + propVar.getOntProperty().getURI() + "> " + propVar.getVariable() + ". \n  ";
            }
        }
        return sparql;
    }

    String getName() {
        return name;
    }

    List<PropVar> getLinkWithInputs() {
        return linkWithInputs;
    }

    List<PropVar> getLinkWithParameters() {
        return linkWithParameters;
    }

    PropVar getCharacterizeInput() {
        return characterizeInput;
    }

    Resource getType() {
        return type;
    }

}
