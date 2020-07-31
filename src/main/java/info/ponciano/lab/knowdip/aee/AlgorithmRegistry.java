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

package info.ponciano.lab.knowdip.aee;

import info.ponciano.lab.knowdip.KD;
import info.ponciano.lab.knowdip.aee.algorithm.Algorithm;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;


/**
 * Loads semantic information about a set of algorithms within a given ontology.
 */
public class AlgorithmRegistry {
	
	private static AlgorithmRegistry instance = null;
	
	private final PropertyFunctionRegistry reg = PropertyFunctionRegistry.get(ARQ.getContext());
	private OntModel model = null;
	
	public AlgorithmRegistry() { }
	
	public static AlgorithmRegistry get() {
		if (instance == null) {
			instance = new AlgorithmRegistry();
		}
		
		return instance;
	}
	
	/**
	 * Reads a package of sub-classes of Algorithm, adds them to the Jena ARQ property function
	 * execution registry and loads semantic information into the parameter model.
	 * 
	 * @param model is the ontology model to be altered
	 * @param packagePath is the java package containing the algorithms
	 * @see info.ponciano.lab.knowdip.execution_engine.aee.Algorithm
	 */
	public void load(OntModel model, String packagePath) throws KnowdipException {
            this.load(model);
		List<ClassLoader> classLoadersList = new LinkedList<>();
		
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());
		
		
		Reflections reflections = new Reflections(packagePath);
		
		Set<Class<? extends Algorithm>> algorithmClasses = reflections.getSubTypesOf(Algorithm.class);
		
		for (Class<? extends Algorithm> algorithmClass : algorithmClasses) {
			register(algorithmClass);
		}
	}
        
        /**
	 * Reads a package of sub-classes of Algorithm, adds them to the Jena ARQ property function
	 * execution registry and loads semantic information into the parameter model.
	 * 
	 * @param model is the ontology model to be altered
	 * @see info.ponciano.lab.knowdip.execution_engine.aee.Algorithm
	 */
	public void load(OntModel model) {
		this.model = model;
	}
	
	
	/**
	 * Registers the Algorithm sub-class within the ARQ engine and adds information about it
	 * to the loaded model.
	 * 
	 * @param clazz is the class inheriting from Algorithm
	 * @return the newly created OntClass
	 * @see info.ponciano.lab.knowdip.execution_engine.aee.Algorithm
	 */
	public OntClass register(Class<? extends Algorithm> clazz) throws KnowdipException {
		OntClass algorithm = model.createClass(KD.NS + clazz.getSimpleName());
		
		reg.put(algorithm.getURI(), clazz);

		return algorithm;
	}
	
	/**
	 * Removes a registered algorithm from the ARQ engine and the loaded ontology model.
	 * 
	 * @param uri is the URI of the algorithm to be removed
	 * @throws NotASubClassOfAlgorithmException in case the algorithm to be removed 
	 * is not an ont sub-class of Algorithm
	 */
	public void unregister(String uri) throws NotASubClassOfAlgorithmException {
		OntClass algorithm = model.getOntClass(uri);
		
		if (algorithm != null) {
			if (!algorithm.hasSuperClass(model.getOntClass(KD.ALGORITHM))) {
				throw new NotASubClassOfAlgorithmException("Ontology class must be of type " 
						+ KD.ALGORITHM + ".");
			}
			
			model.removeAll(algorithm, null, null);
			model.removeAll(null, null, algorithm);
			reg.remove(uri);
		}
	}
	

	/**
	 * Gets the URI of the Algorithm sub-class.
	 * 
	 * @param clazz is the class to get the URI of
	 * @return the URI of the class
	 */
	public static String getAlgorithmURI(Class<? extends Algorithm> clazz) {
		return KD.NS + clazz.getSimpleName();
	}
	
}
