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
import info.ponciano.lab.knowdip.aee.algorithm.LoadCloud;
import info.ponciano.lab.knowdip.aee.algorithm.PatchesSegmentation;
import info.ponciano.lab.pisemantic.PiOntologyException;
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

        args = new String[2];
        args[0] = "src/main/resources/knowdip.owl";
        args[1] = "output/";
        try {
            Knowdip knowdip = new Knowdip(args[0], args[1], true);
            knowdip.add(LoadCloud.class);
            knowdip.add(PatchesSegmentation.class);
            // Load point cloud
            knowdip.interprets("CONSTRUCT{ ?out rdf:type knowdip:FullPointCloud . ?out knowdip:readFrom ?i0.} " + "WHERE{"
                    + "?i0 rdf:type knowdip:PointCloudFile . "
                    + "FILTER NOT EXISTS { ?something knowdip:readFrom ?i0 } . " + "?i0 knowdip:hasSource ?v0 ."
                    + "?out knowdip:LoadCloud( \"hasSource =\" ?v0)" + "}");

            // Segments in patches point cloud
            knowdip.interprets("CONSTRUCT{ ?out rdf:type knowdip:Patches . ?out knowdip:comesFrom ?i0.} " + "WHERE{"
                    + "?i0 rdf:type knowdip:FullPointCloud . "
                    + "FILTER NOT EXISTS { ?something knowdip:comeFrom ?i0 } . "
                    + "?out knowdip:PatchesSegmentation( \"hasInput =\" ?i0)"
                    + "}");

            ResultSet select = knowdip.select("SELECT ?c WHERE{ ?c rdf:type knowdip:FullPointCloud }");
            while (select.hasNext()) {
                QuerySolution next = select.next();
                String uri = next.get("c").asResource().getURI();
                System.out.println(uri);
            }
            knowdip.save();
        } catch (KnowdipException | IOException | PiOntologyException ex) {
            Logger.getLogger(Knowdip.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
