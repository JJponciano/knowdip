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

import info.ponciano.lab.jpc.math.Coord3D;
import info.ponciano.lab.jpc.pointcloud.components.APointCloud;
import info.ponciano.lab.knowdip.Knowdip;
import info.ponciano.lab.knowdip.aee.KnowdipException;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public class SemanticSegmentationExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            args = new String[2];
            args[0] = "src/main/resources/knowdip.owl";
            args[1] = "output/";

            /**
             * 0:ontologyPath 1:output directory 2:reset the output directory
             * 3:using a triple store
             */
            Knowdip.init(args[0], args[1], false, true);

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

            System.out.println("Step 1");
            //Step 1: interprets all SPARQL queries contained in the file to segment the point cloud
            knowdip.interpretsFile("src/main/resources/queries.txt");

            System.out.println("Step 2");
            //Step 2: Groups patches in segments.
            //get patches
            Map<String, APointCloud> patches = knowdip.getPatches();
            patches.forEach((k, v) -> {
                String seguri;
                Iterator<KSolution> segSelect = knowdip.select("SELECT ?s WHERE {?s knowdip:isComposedOf  <" + k + ">}");
                //test if it not exists a segment that is composed of the patch k
                if (!segSelect.hasNext()) {
                    //creates the segment in the triplestore and specify it is composed of the patch k
                    seguri = Knowdip.createURI().getURI();
                    try {
                        knowdip.update("INSERT DATA {<" + seguri + "> rdf:type knowdip:Segment . <" + seguri + "> knowdip:isComposedOf  <" + k + "> }");
                    } catch (KnowdipException ex) {
                        Logger.getLogger(SemanticSegmentationExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    seguri = segSelect.next().get("?s").asResource().getURI();
                }
                mergingPatch(knowdip, k, seguri);
            });

            System.out.println("Display:");
            //select segments
           knowdip.displaySegment("SELECT ?segment WHERE{ ?segment rdf:type knowdip:Segment}", "?segment", true);
        } catch (IOException | KnowdipException | PiOntologyException ex) {
            Logger.getLogger(SemanticSegmentationExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected static void mergingPatch(Knowdip knowdip, String k, String seguri) {
        //for each patch, select  other patches that is in contact with it and not yet in a segment.
        Iterator<KSolution> patchesInContact = knowdip.select("SELECT ?p WHERE{ <" + k + "> knowdip:inContact ?p. FILTER NOT EXISTS {?s knowdip:isComposedOf ?p} }");
        List<String> recursivPatch = new LinkedList<>();
        while (patchesInContact.hasNext()) {
            KSolution next = patchesInContact.next();
            String patchURI = next.get("?p").asResource().getURI();
            //if the patch is similar to the k, it is added to the segment.
            boolean similar = isSimilar(knowdip, patchURI, k, 0.5);
            if (similar) {
                try {
                    knowdip.update("INSERT DATA { <" + seguri + "> knowdip:isComposedOf  <" + patchURI + "> }");
                    //if it is similar, the merging is applied recursively.
                    recursivPatch.add(patchURI);
                } catch (KnowdipException ex) {
                    Logger.getLogger(SemanticSegmentationExample.class.getName()).log(Level.SEVERE, null, ex);
                }
            }// if it is not similar, no recursivity.
        }
        recursivPatch.forEach(patchURI -> mergingPatch(knowdip, patchURI, seguri));
    }

    private static boolean isSimilar(Knowdip knowdip, String patchURI, String k, double d) {
        double x1 = 0, x2 = 0, y1 = 0, y2 = 0, z1 = 0, z2 = 0;
        //to patches are similar if their have the same normal.
        Iterator<KSolution> select = knowdip.select("SELECT ?x ?y ?z WHERE {<" + patchURI + "> knowdip:hasNormalX ?x . "
                + "<" + patchURI + "> knowdip:hasNormalY ?y . "
                + "<" + patchURI + "> knowdip:hasNormalZ ?z . }");
        while (select.hasNext()) {
            KSolution next = select.next();
            x1 = next.get("?x").asLiteral().getDouble();
            y1 = next.get("?y").asLiteral().getDouble();
            z1 = next.get("?z").asLiteral().getDouble();
        }
        select = knowdip.select("SELECT ?x ?y ?z WHERE {<" + k + "> knowdip:hasNormalX ?x . "
                + "<" + patchURI + "> knowdip:hasNormalY ?y . "
                + "<" + patchURI + "> knowdip:hasNormalZ ?z . }");
        while (select.hasNext()) {
            KSolution next = select.next();
            x2 = next.get("?x").asLiteral().getDouble();
            y2 = next.get("?y").asLiteral().getDouble();
            z2 = next.get("?z").asLiteral().getDouble();
        }
        return new Coord3D(x2, y2, z2).distance(new Coord3D(x1, y1, z1)) < d;
    }

}
