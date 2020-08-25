/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
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
package examples;

import info.ponciano.lab.knowdip.Knowdip;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.jena.MinPatchesDistanceEstimation;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.LoadCloud;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.PatchesSegmentation;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchArea;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchColor;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchDensity;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchDistanceX;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchDistanceY;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchDistanceZ;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchMaxZ;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchMinZ;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchNormalX;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchNormalY;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchNormalZ;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchSize;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.getter.GetPatchVolume;
import info.ponciano.lab.knowdip.reasoner.KSolution;
import info.ponciano.lab.knowdip.reasoner.PiOntologyException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.rdf.model.RDFNode;

/**
 *
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public class FileExempleWithTS {

    public static void main(String[] args) {

        try {
            args = new String[2];
            args[0] = "src/main/resources/knowdip.owl";
            args[1] = "output/";
            /**
             *  ontologyPath,output directory, reset the output directory, using a triple store
             */
            Knowdip.init(args[0], args[1], true, true);

            Knowdip knowdip = Knowdip.get();
            knowdip.add(LoadCloud.class);
            knowdip.add(PatchesSegmentation.class);
            knowdip.add(GetPatchSize.class);
            knowdip.add(GetPatchColor.class);
            knowdip.add(GetPatchArea.class);
            knowdip.add(GetPatchDensity.class);
            knowdip.add(GetPatchDistanceX.class);
            knowdip.add(GetPatchDistanceY.class);
            knowdip.add(GetPatchDistanceZ.class);
            knowdip.add(GetPatchMaxZ.class);
            knowdip.add(GetPatchMinZ.class);
            knowdip.add(GetPatchNormalX.class);
            knowdip.add(GetPatchNormalY.class);
            knowdip.add(GetPatchNormalZ.class);
            knowdip.add(GetPatchVolume.class);

            //interprets all SPARQL queries contained in the file
            knowdip.interpretsFile("src/main/resources/queries.txt");

            /**
             * If no distance is found, perhaps they are not calculated.
            */
            System.out.println("Calculate distance between patches");
            if (!knowdip.select("SELECT ?c WHERE{ ?c rdf:type knowdip:Patch . ?c knowdip:has2m ?c2  }").hasNext()) {//Calculate the distance between patches.
                MinPatchesDistanceEstimation mde = new MinPatchesDistanceEstimation();
                mde.run();
            } 
            
            
            Iterator<KSolution> selectString = knowdip.select("SELECT ?c ?z WHERE{ ?c rdf:type knowdip:Patch . ?c knowdip:hasNormalZ ?z . Filter(?z <0.1 ) }");
           if(!selectString.hasNext())System.out.println("No element selected");
           else            {
                //get uri
                String patchuri = selectString.next().get("?c").asResource().getURI();
                //select and display all other cloud that as close to the patch
                System.out.println(knowdip.selectAsText("SELECT ?c2 WHERE {<"+patchuri+"> knowdip:inContact ?c2}"));
           }
            System.out.println(selectString);

            
            knowdip.save();
        } catch (IOException | KnowdipException | PiOntologyException ex) {
            Logger.getLogger(FileExempleWithTS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
