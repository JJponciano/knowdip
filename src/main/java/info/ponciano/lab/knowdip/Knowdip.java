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
package info.ponciano.lab.knowdip;

import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.Algorithm;
import info.ponciano.lab.knowdip.aee.memory.Memory;
import info.ponciano.lab.knowdip.reasoner.KReasoner;
import info.ponciano.lab.knowdip.reasoner.PiOntologyException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.core.Prologue;

/**
 *
 * @author Jean-Jacques Ponciano
 */
public class Knowdip {

    private static Knowdip instance;
    private final KReasoner reasoner;

    public Memory getMemory() {
        return this.reasoner.getMemory();
    }

    public static void init(String ontologyPath, String outDir, boolean reset) throws IOException, KnowdipException, FileNotFoundException, PiOntologyException {
        if (instance == null) {
            instance = new Knowdip(ontologyPath, outDir, reset);
        }
    }

    public static Knowdip get() {
        return instance;
    }

    Knowdip(String ontologyPath, String outDir, boolean reset) throws IOException, KnowdipException, FileNotFoundException, PiOntologyException {

        if (reset) {
            clearAll(outDir);
        }
        this.reasoner = new KReasoner(ontologyPath, outDir);
    }

    public static void clearAll(String outDir) {
        File dir = new File(outDir);
        if (dir.exists()) {
            try {
                if (dir.isDirectory()) {
                    FileUtils.cleanDirectory(dir);
                }
                FileUtils.forceDelete(dir);
            } catch (IOException ex) {
                Logger.getLogger(Knowdip.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Add new algorithm to be registered
     *
     * @param algo algorithm to be registered.
     * @throws info.ponciano.lab.knowdip.KnowdipException
     */
    public void add(Class<? extends Algorithm> algo) throws KnowdipException {
        this.reasoner.add(algo);
    }

    public void update(String query) {
        this.reasoner.update(query);
    }

    /**
     * Interprets every SPARQL query contained in a file
     *
     * The output of the algorithms should has the name "?out".
     *
     * @param pathfile path of the file
     * @return true if all queries produce results, false otherwise
     */
    public boolean interpretsFile(String pathfile) throws IOException, KnowdipException {
        final StringBuilder buff = new StringBuilder();
        final File fileio = new File(pathfile);

        try ( BufferedReader reader = Files.newBufferedReader(fileio.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                buff.append(line).append("\n");
            }
        }
        String[] buffS = buff.toString().split("CONSTRUCT|SELECT");
        //if the query is not empty, interprets it 
        boolean allOK = true;
        for (String query : buffS) {
            if (!query.isEmpty()) {
                if (!this.interprets("CONSTRUCT" + query)) {
                    allOK = false;
                }
            }
        }
        return allOK;
    }

    /**
     * Executes construct with the execution of an algorithms.
     *
     * The output of the algorithms should has the name "?out".
     *
     * @param queryString construct query with asking the execution of an
     * algorithms
     * @return true if queries produces results, false otherwise
     */
    public boolean interprets(String queryString) throws KnowdipException {
            String toUpperCase = queryString.toUpperCase();

            if (!toUpperCase.contains("CONSTRUCT") && !toUpperCase.contains("SELECT") && !queryString.contains("}")) {
                throw new KnowdipException("Impossible to interprets the query " + queryString);
            }
            if (queryString.contains("\n")) {
                throw new KnowdipException("Please remove newline form the query" + queryString);
            }
            return this.reasoner.interprets(queryString);
    }

    /**
     * Executes construct .
     *
     * @param queryString construct query
     * @return true if queries produces results, false otherwise
     * @throws info.ponciano.lab.knowdip.KnowdipException If the query fail.
     */
    public boolean construct(String queryString) throws KnowdipException {
        return this.reasoner.construct(queryString);
    }

    /**
     * Remove every individual corresponding to the variable specify after the
     * {@code REMOVE}.
     * <p>
     * For example for removing every dog: {@code "REMOVE ?c WHERE{ ?c rdf:type dog
     * } } </p>
     *
     * @param query Query to be interpreted
     */
    public void remove(String query) {
        this.reasoner.remove(query);
    }

    public void save() {
        this.reasoner.close();

    }

    public static Node createURI() {
        return NodeFactory.createURI(KD.NS + UUID.randomUUID().toString());
    }

    public static Node createURI(String name) {
        if (!name.contains("#")) {
            name = KD.NS + name;
        }
        return NodeFactory.createURI(name);
    }

    /**
     * Execute a SPARQL select query.
     *
     * @param queryString query to be executed
     * @return the resultset of the query
     */
    public ResultSet select(String query) {
        return this.reasoner.select(query);
    }

    public String selectAsText(String query) {
        return ResultSetFormatter.asText(this.select(query), new Prologue(this.reasoner.getWorkingModel()));
    }

    public Property getProperty(String HAS_VALUE) {
        return this.reasoner.getModel().getProperty(HAS_VALUE);
    }

    public List<String> listPointClouds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
