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
package info.ponciano.lab.knowdip.aee.algorithm.jena;

import info.ponciano.lab.jpc.pointcloud.components.APointCloud;
import info.ponciano.lab.knowdip.Knowdip;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 * {@code MinPatchesDistanceEstimation } aims to calculate the minimum distance
 * between all patches stored in the ontology according to their oriented
 * bounding box.
 *
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public class MinPatchesDistanceEstimation implements Runnable {

    @Override
    public void run() {
        //get all patches
        List<APointCloud> patches = this.getPatches();
        //calculate the minimum distance between all patches in multi-threading
        List<info.ponciano.lab.jpc.algorithms.MinPatchesDistanceEstimation> workers = new ArrayList<>();
        for (int i = 0; i < patches.size() - 1; i++) {
            for (int j = i + 1; j < patches.size(); j++) {
                workers.add(new info.ponciano.lab.jpc.algorithms.MinPatchesDistanceEstimation(patches.get(i), patches.get(j)));
            }
        }
        //executes all thread
        ExecutorService execute = Executors.newCachedThreadPool();
        workers.forEach(w->execute.submit(w));

    }

    private List<APointCloud> getPatches() {

        List<APointCloud> patches = new ArrayList<>();
        ResultSet select = Knowdip.get().select("SELECT ?p WHERE{ ?p rdf:type knowdip:Patch}");
        while (select.hasNext()) {
            //get URI of the patch
            QuerySolution next = select.next();
            String uri = next.get("p").asResource().getURI();
            //retrieve the patch in the memory
            APointCloud access = (APointCloud) Knowdip.get().getMemory().access(uri);
            patches.add(access);
        }
        return patches;
    }

}
