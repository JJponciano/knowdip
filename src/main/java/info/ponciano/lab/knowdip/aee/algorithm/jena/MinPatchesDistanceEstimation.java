/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano (Contact: jean-jacques@ponciano.info)
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

import info.ponciano.lab.jpc.algorithms.segmentation.PatchesDistanceEstimation;
import info.ponciano.lab.jpc.pointcloud.components.APointCloud;
import info.ponciano.lab.knowdip.Knowdip;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@code MinPatchesDistanceEstimation } aims to calculate the minimum distance
 * between all patches stored in the ontology according to their oriented
 * bounding box.
 *
 * @author Dr Jean-Jacques Ponciano (Contact: jean-jacques@ponciano.info)
 */
public class MinPatchesDistanceEstimation extends PatchesDistanceEstimation {

    @Override
    protected void postprocessing(List<info.ponciano.lab.jpc.algorithms.segmentation.MinPatchesDistanceEstimation> workers, Map<String, APointCloud> patches) {
        System.out.println(new Date(System.currentTimeMillis()).toLocaleString() + " postprocessing " + this.getClass().getName());

        List<String> updateQueries = new ArrayList<>();
        workers.forEach(w -> {
            APointCloud patch1 = w.getPatch1();
            APointCloud patch2 = w.getPatch2();
            Double results = w.getResults();
            //retrieve URI
            Iterator<String> iterator = patches.keySet().iterator();
            String uri1 = "";
            String uri2 = "";
            while ((uri1.isEmpty() || uri2.isEmpty()) && iterator.hasNext()) {
                String k = iterator.next();
                if (uri1.isEmpty() && patches.get(k).equals(patch1)) {
                    uri1 = k;
                } else if (uri2.isEmpty() && patches.get(k).equals(patch2)) {
                    uri2 = k;
                }
            }
            String property = getDistanceProperty(results);
            if (!property.isEmpty()) {
                updateQueries.add("INSERT DATA {<" + uri1 + "> knowdip:" + property + " <" + uri2 + "> }");
            }
        });
        System.out.println(new Date(System.currentTimeMillis()).toLocaleString() + " Update knowledge base with " + updateQueries.size() + " queries" + this.getClass().getName());

        Knowdip.get().update(updateQueries);
        System.out.println(new Date(System.currentTimeMillis()).toLocaleString() + " Update done!  " + this.getClass().getName());

    }

    @Override
    protected Map<String, APointCloud> getPatches() {
        return Knowdip.get().getPatches();
    }

    private String getDistanceProperty(Double results) {
        if (results < 0.01) {
            return "inContact";
        } else if (results < 0.1) {
            return "isClose";
        } else if (results < 1) {
            return "isInTheVicinityOf ";
        } else if (results <= 20) {
            return "has" + Math.round(results) + "m";
        } else if (results < 100) {
            return "has" + Math.round(results / 10.0) + "0m";
        } else if (results < 1001) {
            return "has" + Math.round(results / 100.0) + "00m";
        }
        return "";
    }

}
