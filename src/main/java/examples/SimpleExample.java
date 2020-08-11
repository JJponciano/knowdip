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
import info.ponciano.lab.knowdip.reasoner.PiOntologyException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 *
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public class SimpleExample {

    public static void main(String[] args) {

        try {
            args = new String[2];
            args[0] = "src/main/resources/knowdip.owl";
            args[1] = "output/";
            try {
                Knowdip.init(args[0], args[1], true);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PiOntologyException ex) {
                Logger.getLogger(SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            // Load point cloud
            knowdip.interprets("CONSTRUCT{ ?out rdf:type knowdip:FullPointCloud . ?out knowdip:readFrom ?i0.} " + "WHERE{"
                    + "?i0 rdf:type knowdip:PointCloudFile . "
                    + "FILTER NOT EXISTS { ?something knowdip:readFrom ?i0 } . " + "?i0 knowdip:hasSource ?v0 ."
                    + "?out knowdip:LoadCloud( \"hasSource =\" ?v0)" + "}");
            // Segments in patches point cloud
            knowdip.interprets("CONSTRUCT{ ?out rdf:type knowdip:Patch . ?out knowdip:comesFrom ?i0.} " + "WHERE{"
                    + "?i0 rdf:type knowdip:FullPointCloud . "
                    + "FILTER NOT EXISTS { ?something knowdip:comesFrom ?i0 } . "
                    + "?out knowdip:PatchesSegmentation( \"hasInput =\" ?i0)"
                    + "}");

            /*
             * Features extraction
             */
            // Get the size of each patch
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasSize ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasSize ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchSize( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasColor ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasColor ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchColor( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasArea ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasArea ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchArea( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasDensity ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasDensity ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchDensity( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasDistanceX ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasDistanceX ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchDistanceX( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasDistanceY ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasDistanceY ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchDistanceY( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasDistanceZ ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasDistanceZ ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchDistanceZ( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasMaxZ ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasMaxZ ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchMaxZ( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasMinZ ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasMinZ ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchMinZ( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");

            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasNormalX ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasNormalX ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchNormalX( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasNormalY ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasNormalY ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchNormalY( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasNormalZ ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasNormalZ ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchNormalZ( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");
            knowdip.interprets("CONSTRUCT{ ?p knowdip:hasVolume ?out } " + "WHERE{"
                    + "?p rdf:type knowdip:Patch . "
                    + "FILTER NOT EXISTS { ?p knowdip:hasVolume ?s } . "
                    + "?p knowdip:comesFrom ?i0 . "
                    + "?out knowdip:GetPatchVolume( \"hasInput =\" ?i0 \"patchID =\" ?p)"
                    + "}");

            //Calculate the distance between patches.
            MinPatchesDistanceEstimation mde = new MinPatchesDistanceEstimation();
//            mde.run();

            ResultSet select = knowdip.select("SELECT ?c WHERE{ ?c rdf:type knowdip:FullPointCloud }");
            while (select.hasNext()) {
                QuerySolution next = select.next();
                String uri = next.get("c").asResource().getURI();
                System.out.println(uri);
            }
            knowdip.save();
        } catch (IOException ex) {
            Logger.getLogger(SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KnowdipException ex) {
            Logger.getLogger(SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
