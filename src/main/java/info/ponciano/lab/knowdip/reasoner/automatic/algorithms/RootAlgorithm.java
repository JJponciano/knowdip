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
import info.ponciano.lab.knowdip.reasoner.automatic.SemParsingObject;
import info.ponciano.lab.knowdip.KD;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiRestrictions;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntClass;

/**
 *
 * @author Jean-Jacques
 */
 class RootAlgorithm  extends SemParsingObject {

    //restriction about algorithms class
    protected PiRestrictions inputs;
    protected PiRestrictions outputs;
    protected PiRestrictions parameters;
    protected PiRestrictions relevantTo;
    protected PiRestrictions dataOutputs;
    

     RootAlgorithm(OntClass origin, PiOnt piont) {
        super(origin, piont);
           this.init();
    }
  PiRestrictions getInputs() {
        return inputs;
    }

     PiRestrictions getOutputs() {
        return outputs;
    }

     PiRestrictions getParameters() {
        return parameters;
    }

    private void init() {
        try {
            this.inputs = this.filteringIsSub(KD.HAS_INPUT);
            this.outputs = this.filteringIsSub(KD.HAS_OUTPUT);
            this.dataOutputs = this.filteringIsSub(KD.HAS_DATA_OUTPUT);
            this.parameters = this.filteringIsSub(KD.HAS_CHARA);
            //  this.prerequisites = this.filteringIsSub(piont.getObjectProperty(KD.HAS_PREREQUISITE));
            this.relevantTo = this.filteringIsSub(KD.IS_RELEVANT_TO);
        } catch (KnowdipException ex) {
            Logger.getLogger(RootAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
