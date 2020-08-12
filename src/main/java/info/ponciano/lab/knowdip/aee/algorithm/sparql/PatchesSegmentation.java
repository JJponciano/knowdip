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
package info.ponciano.lab.knowdip.aee.algorithm.sparql;

import info.ponciano.lab.jpc.pointcloud.Pointcloud;
import info.ponciano.lab.jpc.pointcloud.components.APointCloud;
import info.ponciano.lab.knowdip.KD;
import info.ponciano.lab.knowdip.Knowdip;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.Algorithm;
import info.ponciano.lab.knowdip.aee.memory.WritableResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class PatchesSegmentation extends Algorithm {

    Pointcloud hasInput;
    double hasMaxSize;

    @Override
    protected Iterable<Node> process() throws KnowdipException {
        List<Node> nodes = new ArrayList<>();
//        if (!data.getClass().equals(Pointcloud.class)) {
//            System.err.println("hasInput is not a point cloud in info.ponciano.lab.knowdip.aee.algorithm.mappc.PatchesSegmentation.process()");
//        } else {
//            Pointcloud pc = (Pointcloud) data;

            Map<String, APointCloud> patches = hasInput.getPatches();
            patches.forEach((String k, APointCloud cloud) -> {
                Node node = Knowdip.createURI(k);
                nodes.add(node);
            });
            System.out.println("Patches: " + nodes.size());
//        }
        return nodes;

    }

}
