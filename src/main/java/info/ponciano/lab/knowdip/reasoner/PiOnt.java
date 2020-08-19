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
package info.ponciano.lab.knowdip.reasoner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;

/**
 *
 * @author Jean-Jacques Ponciano
 */
public class PiOnt {

    public static String NS_SEMANTIC = "http://lab.ponciano.info/pisemantic#";
    protected String ns;// default ns
    /**
     * Ontology.
     */
    protected OntModel ont;

    /**
     * Creates instance of PiOntology
     *
     * @param ont ontology model.
     */
    public PiOnt(OntModel ont) throws FileNotFoundException {
        this.ns = NS_SEMANTIC;
        this.ont = ont;
    }

    /**
     * Creates instance of PiOntology
     *
     * @param path Path of the ontology to be load.
     */
    public PiOnt(final String path) throws FileNotFoundException {
        this.ns = NS_SEMANTIC;
        this.ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // creation of an ontological model
        this.read(path);
    }

    public String getNs() {
        return ns;
    }

    public void setNs(final String ns) {
        this.ns = ns;
    }

    /**
     * Creates instance of PiOntology
     *
     */
    public PiOnt() {
        this.ns = "";
        // creation of an ontological model
        this.ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    }

    // **************************************************************************
    // ---------------------------- CREATION -----------------------------------
    // **************************************************************************
    /**
     * Creates a <code>OntClass</code> in the ontology if it does not exist.
     *
     * @param name <code>OntClass</code> name with the name space.
     * @return <code>OntClass</code> created in the ontology or get if it
     * already exists.
     */
    // @Override
    public OntClass createClass(String name) {
        name = this.getURI(name);
        OntClass ontClass = this.getOntClass(name);
        if (ontClass == null) {
            ontClass = this.ont.createClass(name);
        }
        return ontClass;
    }

    /**
     * Test if the value contains namespace, if it not contains, add its the
     * value.
     *
     * @param value value to test
     * @return correct formed URI with the default namespace if necessary.
     */
    private String getURI(final String value) {
        if (value.contains("#")) {
            return value;
        } else {
            return this.ns + value;
        }
    }

    /**
     * Creates <code>Resource</code> in the ontology if it does not exist.
     *
     * @param name resource name
     * @return the <code>Resource</code> created or get if it already exists.
     */
    // @Override
    public Resource createResource(String name) {
        name = this.getURI(name);
        final Resource resource = this.ont.getResource(name);
        if (resource == null) {
            return this.ont.createResource(name);
        }
        return resource;
    }

    /**
     * Creates anonymous <code>Resource</code>
     *
     * @return the <code>Resource</code> createNewFile.
     */
    // @Override
    public Resource createResource() {
        final Resource createResource = this.ont.createResource(this.ns + UUID.randomUUID().toString());
        return createResource;
    }

    /**
     * Creates <code>DatatypeProperty</code> if there does not exist.
     *
     * @param name property name .
     * @return the <code>DatatypeProperty</code> created or the property found
     * if it is already existed.
     */
    // @Override
    public DatatypeProperty createDatatypeProperty(String name) {
        name = this.getURI(name);
        DatatypeProperty data = this.ont.getDatatypeProperty(name);
        if (data == null) {
            data = this.ont.createDatatypeProperty(name);
        }
        return data;
    }

    /**
     * Creates <code>ObjectProperty</code> if it does not exist
     *
     * @param name name of the property .
     * @return the <code>ObjectProperty</code> created or the property found if
     * it is already existed.
     */
    // @Override
    public ObjectProperty createObjectProperty(String name) {
        name = this.getURI(name);
        ObjectProperty object = this.ont.getObjectProperty(name);
        if (object == null) {
            object = this.ont.createObjectProperty(name);
        }
        return object;
    }

    // **************************************************************************
    /**
     * Show all RDF data in the output system.
     */
    // @Override
    public void showFile() {
        this.ont.write(System.out);
    }

    /**
     * Write all RDF data in a file
     *
     * @param path path of the file will be erase or create.
     * @param ont model to be written
     * @return true if the file is safe, false otherwise.
     */
    // @Override
    public static boolean write(String path, final OntModel ont) {
        path = path.substring(path.lastIndexOf('.'), path.length() - 1);
        path += ".owl";
        BufferedOutputStream out = null;
        boolean isOk = true;
        try {
            // initialization
            out = new BufferedOutputStream(new FileOutputStream(new File(path)));
            // RDFDataMgr.write(out, ont, RDFFormat.TURTLE);
            ont.write(out);

            out.close();
        } catch (final IOException ex) {
            Logger.getLogger(PiOnt.class.getName()).log(Level.SEVERE, null, ex);
            isOk = false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (final IOException ex) {
                Logger.getLogger(PiOnt.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return isOk;

    }

    /**
     * Adds a prefix.
     *
     * @param prefix short prefix key ( knowdip)
     * @param ns Namespace use for the prefix
     * (http://lab.ponciano.info/knowdip#)
     */
    public void addNsPrefix(final String prefix, final String ns) {
        this.ont.setNsPrefix(prefix, ns);
    }

    // **************************************************************************
    // ---------------------------- GETTER -----------------------------------
    // **************************************************************************
    /**
     * * Gets <code>DatatypeProperty</code> from the name and the name space.
     *
     * @param name name of the property .
     * @return the <code>DatatypeProperty</code> found or null if the property
     * does not exist.
     */
    // @Override
    public DatatypeProperty getDataProperty(String name) {
        name = this.getURI(name);
        return this.ont.getDatatypeProperty(name);
    }

    /**
     * Gets <code>ObjectProperty</code> from the name and the name space
     *
     * @param name name of the property .
     * @return the <code>ObjectProperty</code> found or null if the property
     * does not exist.
     */
    // @Override
    public ObjectProperty getObjectProperty(String name) {
        name = this.getURI(name);
        return this.ont.getObjectProperty(name);
    }

    /**
     * Gets <code>OntProperty</code> from the name and the name space.
     *
     * @param name name of the property .
     * @return the <code>getOntProperty</code> found or null if the property
     * does not exist.
     */
    public OntProperty getOntProperty(String name) {
        name = this.getURI(name);
        return this.ont.getOntProperty(name);
    }

    /**
     * Gets <code>OntClass</code> from the name.
     *
     * @param name class name with the name space.
     * @return the <code>OntClass</code> found or null if the class does not
     * exist.
     * @see getOntClass( String name).
     */
    // @Override
    public OntClass getOntClass(String name) {
        name = this.getURI(name);
        return this.ont.getOntClass(name);
    }

    /**
     * Gets <code>Resource</code> from the name.
     *
     * @param name <code>Resource</code> name
     * @return the <code>Resource</code> found or null if the class does not
     * exist.
     */
    // @Override
    public Resource getResource(String name) {
        name = this.getURI(name);
        return this.ont.getResource(name);
    }
    // **************************************************************************

    /**
     * Adds individual in the own base and add every property with value at this
     * individual.
     *
     * @param name name of the individual .
     * @param model model used to create the individual.
     * @param properties properties of the individual.
     * @param values list of each value corresponding at each properties.
     * @throws PiOntologyException if the length of the both list are not
     * equals.
     *
     * <h3>Example</h3>
     * <p>
     * PiOntology ont = new PiOntology(); String name = "Chien"; OntClass model
     * = ont.createClass(name); List values = new ArrayList<>(); List properties
     * = new ArrayList<>();
     * properties.add(ont.createDatatypeProperty("hasFamily"));
     * properties.add(ont.getDataProperty("hasFamily"));
     * properties.add(ont.getDataProperty("hasFamily"));
     * properties.add(ont.getDataProperty("hasFamily"));
     * values.add(ont.createResource("Claire"));
     * values.add(ont.createResource("Perle"));
     * values.add(ont.createResource("Plume"));
     * values.add(ont.createResource("JJ")); PiOntology instance = new
     * PiOntology(); instance.addIndividualDataPrprt("Dana", model, values,
     * properties);
     * </p>
     */
    // @Override
    public void addIndividualDataPrprt(final String name, final OntClass model, final List<RDFNode> values,
            final List<Property> properties) throws PiOntologyException {
        final int length = properties.size();
        if (length != values.size()) {
            throw new PiOntologyException(
                    "The size of the both list in" + this.getClass().toString() + ".addIndividual are differents.");
        } // create the individual
        final Resource a1 = this.createIndividual(name, model);
        for (int i = 0; i < length; i++) {
            if (values.get(i).isLiteral()) {
                a1.addLiteral(properties.get(i), values.get(i));
            } else {
                a1.addProperty(properties.get(i), values.get(i));
            }
        }
    }

    /**
     * Creates restriction class
     *
     * @param property property which the restriction will be applied on.
     * @param restrictionType type of the restriction applied
     * (<code>OWL</code>).
     * @param appliedRestriction class which the restriction will be applied on.
     * @return the restriction class created
     */
    public OntClass createRestriction(final ObjectProperty property, final Property restrictionType,
            final Resource appliedRestriction) {
        return this.createRestriction(property, restrictionType, appliedRestriction,
                ("rest" + this.ns + UUID.randomUUID().toString()));
    }

    public OntClass createRestriction(final ObjectProperty property, final Property restrictionType,
            final Resource appliedRestriction, final String name) {
        OntClass rest;
        if (restrictionType.equals(OWL.allValuesFrom)) {
            final AllValuesFromRestriction avf = this.ont.createAllValuesFromRestriction(null, property,
                    appliedRestriction);
            rest = avf;
        } else if (restrictionType.equals(OWL.someValuesFrom)) {
            final SomeValuesFromRestriction avf = this.ont.createSomeValuesFromRestriction(null, property,
                    appliedRestriction);
            rest = avf;
        } else if (restrictionType.equals(OWL.hasValue)) {
            final HasValueRestriction avf = this.ont.createHasValueRestriction(null, property, appliedRestriction);
            rest = avf;
        } else {
            // creation of a restriction class as an empty node
            rest = this.createClass(name);
            rest.addProperty(RDFS.subClassOf, OWL2.Restriction);
            // the restriction of en2 is on the property hasInput
            rest.addProperty(OWL2.onProperty, property);
            rest.addProperty(restrictionType, appliedRestriction);
            // this.ont.createRestriction(restrictionType)
        }

        return rest;
    }

    /**
     * Answer a class description defined as the class of those individuals that
     * have exactly the given number of values for the given property, all
     * values of which belong to the given class.
     *
     * @param uri The optional URI for the restriction, or null for an anonymous
     * restriction (which should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The cardinality of the property
     * @param clas The class to which all values of the restricted property
     * should belong
     * @return A new resource representing a cardinality-q restriction
     *
     */
    // @Override
    public CardinalityRestriction createCardinalityQRestriction(final String uri, final Property prop,
            final int cardinality, final Resource clas) {
        final CardinalityRestriction klass = this.ont.createCardinalityRestriction(uri, prop, cardinality);
        klass.removeAll(OWL.cardinality);
        klass.addLiteral(OWL2.qualifiedCardinality, cardinality);
        klass.addProperty(OWL2.onClass, clas);

        return klass;
    }

    public List<Resource> ask(final String queryString, final List<String> ResourceCode) {
        // initialyze the list which will contain tools
        final List<Resource> results = new ArrayList<>();

        final Query query = QueryFactory.create(queryString);
        // apply the query
        try (QueryExecution qe = QueryExecutionFactory.create(query, this.ont)) {
            // execute the query
            final ResultSet result = qe.execSelect();

            // retrieve each line of result and write in a list
            for (; result.hasNext();) {
                final QuerySolution soln = result.nextSolution();
                ResourceCode.stream().map((rc) -> soln.getResource(rc)).forEachOrdered((tool) -> {
                    results.add(tool);
                });
            }
        } catch (final Exception e) {
            System.err.println("Error in PiOntology with the querry:" + queryString + "\n" + e);
        }
        return results;
    }

    /**
     * Executes SPARQL select and return result as resources
     *
     * @param queryString SPARQL query to select resources
     * @param ResourceCode SPARQL variable code to select resources (such as
     * "?s")
     * @return List of resources found.
     */
    public List<Resource> ask(final String queryString, final String ResourceCode) {
        // initialyze the list which will contain tools
        final List<Resource> results = new ArrayList<>();

        final Query query = QueryFactory.create(queryString);
        // apply the query
        try (QueryExecution qe = QueryExecutionFactory.create(query, this.ont)) {
            // execute the query
            final ResultSet result = qe.execSelect();

            // retrieve each line of result and write in a list
            for (; result.hasNext();) {
                final QuerySolution soln = result.nextSolution();
                final Resource tool = soln.getResource(ResourceCode);
                results.add(tool);

            }
        } catch (final Exception e) {
            System.err.println("Error in PiOntology with the querry:" + queryString + "\n" + e);
        }
        return results;
    }

    // @Override
    public OntModel getOnt() {
        return ont;
    }

    public void setOnt(final OntModel ont) {
        this.ont = ont;
    }

    /**
     * Writes the ontology in a file.
     *
     * @param path path of
     * @return the file true if the file is well written
     */
    // @Override
    public boolean write(final String path) {

        return PiOnt.write(path, ont);
    }

    /**
     * Reads the ontology from a file.
     *
     * @param path path of the file.
     */
    public final void read(final String path) throws FileNotFoundException {
        if (!new File(path).exists()) {
            throw new FileNotFoundException(path);
        }
        this.ont.read(path);
        // this.ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
        // FileManager.get().loadModel(path));
        // this.ont = (OntModel) RDFDataMgr.loadModel(path);
    }

    /**
     * Answer a class description defined as the class of those individuals for
     * which all values of the given property belong to the given class
     *
     * @param uri - The optional URI for the restriction, or null for an
     * anonymous restriction (which should be the normal case)
     * @param prop - The property the restriction applies to
     * @param cls - The class to which any value of the property belongs
     *
     * @return A new resource representing an all-values-from restriction
     */
    // @Override
    public AllValuesFromRestriction createAllValuesFromRestriction(final String uri, final Property prop,
            final Resource cls) {
        final AllValuesFromRestriction createAllValuesFromRestriction = this.ont.createAllValuesFromRestriction(uri,
                prop, cls);

        return createAllValuesFromRestriction;
    }

    /**
     * Say if a class has a restriction all value from
     *
     * @param subject class to be tested
     * @param onProperty property of the all value from restriction.
     * @param value value of the all value from restriction.
     * @return true if the class subject has the restriction described, false
     * otherwise.
     */
    public boolean hasAllValuesFromRestriction(final OntClass subject, final Property onProperty,
            final OntClass value) {
        for (final Iterator<OntClass> i = subject.listSuperClasses(true); i.hasNext();) {
            final OntClass c = i.next();

            if (c.isRestriction()) {
                final Restriction r = c.asRestriction();

                if (r.isAllValuesFromRestriction()) {
                    final AllValuesFromRestriction av = r.asAllValuesFromRestriction();
                    final OntProperty iproperty = av.getOnProperty();
                    final Resource ivalue = av.getAllValuesFrom();
                    if (iproperty.equals(onProperty) && ivalue.equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets all subclasses of the class given which has the right property on
     * the right object.
     *
     * @param mother mother class contained subclasses will be used-
     * @param onProperty property that the subclass must to have
     * @param object object of the property that the subclass must have or null
     * if it is not necessary.
     * @param direct If true, only answer the directly adjacent classes in the
     * super-class relation: i.e. eliminate any class for which there is a
     * longer route to reach that child under the super-class relation.
     * @return subclasses have the property <code>onProperty</code> on the
     * object<code>object</code>.
     */
    // @Override
    public List<OntClass> getSubClass(final OntClass mother, final Property onProperty, final RDFNode object,
            final boolean direct) {
        final List<OntClass> subclasses = new ArrayList<>();

        // get each algorithm sub classes
        final ExtendedIterator<OntClass> algos = mother.listSubClasses(direct);
        while (algos.hasNext()) {
            // ges the algorithm
            final OntClass c = algos.next();
            // if the ontclass class has the restriction:
            // onProperty allValueFrom object.
            final boolean hasGoodRest = this.hasRestriction(c, onProperty, object);
            if (hasGoodRest) {
                // the right class is found
                subclasses.add(c);
            }
        }
        return subclasses;
    }

    /**
     * Gets all subclasses of the class given
     *
     * @param mother Class used to get subclasses
     * @param direct If true, only answer the directly adjacent classes in the
     * sub-class relation: i.e. eliminate any class for which there is a longer
     * route to reach that child under the sub-class relation
     * @return List of all subclasses of the given class
     * @throws info.ponciano.lab.pisemantic.PiOntologyException
     */
    // @Override
    public List<OntClass> getSubClass(final OntClass mother, final boolean direct) throws PiOntologyException {
        final List<OntClass> result = new ArrayList<>();
        final ExtendedIterator<OntClass> subClasses = mother.listSubClasses(false);
        if (subClasses != null) {
            final List<OntClass> subL = subClasses.toList();
            result.addAll(subL);
            if (!direct) {
                for (final OntClass subClasse : subL) {
                    result.addAll(this.getSubClass(subClasse, direct));
                }
            }
        }
        return result;
    }

    /**
     * Gets all individuals of a <code>OntClass</code> which have the right
     * property on the right object.
     *
     * @param subject class contained individuals will be tested or null to test
     * every individuals.
     * @param onProperty property that individuals must have.
     * @param object object of the property that individuals must have.
     * @return individuals which have the property <code>onProperty</code> on
     * the object <code>object</code>.
     */
    // @Override
    public List<Individual> getIndividuals(final Resource subject, final ObjectProperty onProperty,
            final Individual object) {
        final List<Individual> res = new ArrayList<>();
        List<Individual> individuals;
        if (subject != null) {
            individuals = this.ont.listIndividuals(subject).toList();
        } else {
            individuals = this.ont.listIndividuals().toList();
        }
        individuals.stream().filter((ind) -> (ind.hasProperty(onProperty, object))).forEach((ind) -> {
            res.add(ind);
        }); // test if the individual has the rigth property on the right object.
        return res;
    }

    public List<Individual> getIndividuals(final Resource subject, final DatatypeProperty onProperty, final String s) {
        final List<Individual> res = new ArrayList<>();
        List<Individual> individuals;
        if (subject != null) {
            individuals = this.ont.listIndividuals(subject).toList();
        } else {
            individuals = this.ont.listIndividuals().toList();
        }
        individuals.stream().filter((ind) -> (ind.hasProperty(onProperty, s))).forEach((ind) -> {
            res.add(ind);
        }); // test if the individual has the rigth property on the right object.
        return res;
    }

    /**
     * check is the indivual satisfy every own restriction
     *
     * @param individual individual to be checked
     * @return true of the individual satisfy every restriction, false
     * otherwise.
     */
    // @Override
    public boolean isConsistent(final Individual individual) {
        // gets restrictions
        return this.checkRestrictions(individual, this.getRestrictions(individual.getOntClass()));
    }

    /**
     * Check if a individual satisfy every restriction
     *
     * @param individual individual to be checked
     * @param restriction restrictions to be checked
     * @return true of the individual satisfy every restriction, false
     * otherwise.
     */
    // @Override
    public boolean checkRestrictions(final Individual individual, final PiRestrictions restriction) {
        return restriction.toList().stream()
                .noneMatch((restriction1) -> (!this.checkRestriction(individual, restriction1)));
    }

    /**
     * Tests if the individual satisfy the restriction given
     *
     * @param individual individual to be tested
     * @param restriction restriction use to test the individual
     * @return true if the individual satisfy the restriction, false otherwise.
     */
    // @Override
    public boolean checkRestriction(final Individual individual, final PiRestriction restriction) {
        final OntProperty onProperty = restriction.getProperty();
        return individual.hasProperty(onProperty);

    }

    /**
     * Gets all restriction for a class
     *
     * @param subject class used to get restriction.
     * @return every restriction about the class given.
     */
    public PiRestrictions getRestrictions(final OntClass subject) {
        return this.getRestrictions(subject, null);
    }

    /**
     * Gets all restrictions of a subject which have the right property.
     *
     * @param subject subject used. it corresponds to the restricted class
     * @param onProperty Property that the subject must have of null to not
     * filter
     * @return list of all restrictions of the subject with have the right
     * property.
     */
    // @Override
    public PiRestrictions getRestrictions(final OntClass subject, final Property onProperty) {
        final boolean direct = true;/*
                                     * If true, only answer the directly adjacent classes in the super-class
                                     * relation: i.e. eliminate any class for which there is a longer route to reach
                                     * that child under the super-class relation.
         */
        final PiRestrictions res = new PiRestrictions();
        final PiRestrictions parentsRest = new PiRestrictions();
        // adds superclass restriction
        final List<OntClass> listSuperClasses = subject.listSuperClasses(direct).toList();
        // adds equivalent class restriction

        listSuperClasses.addAll(subject.listEquivalentClasses().toList());
        listSuperClasses.forEach((c) -> {
            if (c.isRestriction()) {
                final Restriction restri = c.asRestriction();
                if (onProperty == null || restri.getOnProperty().equals(onProperty)) {
                    res.add(new PiRestriction(restri));
                }
            } else {
                // get all superclass restriction
                parentsRest.addAll(this.getRestrictions(c, onProperty));
            }
        });
        // add all parents restrictions if there was not overrided
        parentsRest.toList().stream().filter((pir) -> (!res.containsSameProperty(pir))).forEachOrdered((pir) -> {// if
            // some
            // super
            // class
            // restriction
            // are
            // overide
            // by
            // the
            // class
            res.add(pir);
        });
        return res;
    }

    /**
     * Gets all restrictions which have the right property.
     *
     * @param onProperty Property that restriction has to be applied
     * @return list of all restrictions which have the right property.
     */
    public List<Restriction> getRestrictions(final ObjectProperty onProperty) {
        final List<Restriction> res = new ArrayList<>();
        final List<Restriction> restrictions = this.ont.listRestrictions().toList();
        restrictions.stream().filter((restriction) -> (restriction.getOnProperty().equals(onProperty)))
                .forEachOrdered((restriction) -> {
                    res.add(restriction);
                });
        return res;

    }

    /**
     * List all individuals of a class
     *
     * @param ontclass class contained individuals.
     * @return all indivicuals of the class given.
     */
    // @Override
    public List<Individual> getIndividuals(final Resource ontclass) {
        return this.ont.listIndividuals(ontclass).toList();
    }

    /**
     * Creates individual of <code>subject</code> class in order to populate
     * this class with the property <code>onProperty</code> or update individual
     * of the class if it already existes.
     *
     * @param subject class will be populated
     * @param onProperty property added to the individual created or updated
     * @param objectProperty object of the property.
     * @return true if the individual is create or update, false if it already
     * existe a individual with the property <code>onProperty</code>.
     */
    public boolean populate(final OntClass subject, final ObjectProperty onProperty, final Individual objectProperty) {
        boolean update = false;
        for (final Individual ind : this.getIndividuals(subject)) {
            // test if the individual already has the right property
            final boolean hasProperty = ind.hasProperty(onProperty);
            // if the individual has not this property
            if (!hasProperty) {
                // add property
                ind.addProperty(onProperty, objectProperty);
                update = true;
            }
        }
        // if nothing happens and it is not exist an algorithm with the property
        if (!update && this.getIndividuals(subject, onProperty, objectProperty).isEmpty()) {
            // creates individual with the data or object in input.
            final Individual ind = subject
                    .createIndividual(this.ns + subject.getLocalName() + "_" + UUID.randomUUID().toString());
            // add property
            ind.addProperty(onProperty, objectProperty);
            update = true;
        }
        if (update) {

        }
        return update;
    }

    /**
     * List all individuals which are a value of the specified property .
     *
     * @param subjectClassProperty Class contained individuals that are subject
     * of the property
     * @param onProperty property used to select individuals
     * @return list of property values with a instance of the class as subject.
     */
    public List<Individual> listPropertyValuesFromClass(final OntClass subjectClassProperty,
            final OntProperty onProperty) {
        final List<Individual> inds = new ArrayList<>();
        // get the individual of Run
        final List<Individual> individuals = this.getIndividuals(subjectClassProperty);
        individuals.stream().map((run) -> run.listPropertyValues(onProperty)).forEachOrdered((listPropertyValues) -> {
            // fill the list of algorithm
            while (listPropertyValues.hasNext()) {
                final Individual next = listPropertyValues.next().as(Individual.class);
                inds.add(next);
            }
        });
        return inds;
    }

    /**
     * Lists every @code{OntClass} contained inside the model including owl and
     * rdf classes
     *
     * @return @code{List} of every @code{OntClass}
     */
    public List<OntClass> listClasses() {
        return this.listClasses(null);
    }

    /**
     * Lists every @code{OntClass} contained inside the model that has the same
     * namespace than the namespace given.
     *
     * @param namespace namespace use to filter classes.
     * @return @code{List} of every @code{OntClass}
     */
    public List<OntClass> listClasses(final String namespace) {
        final List<OntClass> result = new ArrayList<>();
        final ExtendedIterator<OntClass> listClasses = this.ont.listClasses();
        final List<OntClass> toList = listClasses.toList();
        int i = 0;
        while (listClasses.hasNext()) {
            System.out.println(i++);
            final OntClass next = listClasses.next();
            if (next != null && next.getLocalName() != null && !next.getLocalName().equals("Thing")
                    && !next.getLocalName().equals("Nothing")) // if the ontclass has the right namespace
            {
                System.out.println(next.getLocalName());
                final String nameSpace = next.getNameSpace();
                if (namespace == null) {
                    result.add(next);
                } else if (nameSpace.equals(namespace)) {
                    result.add(next);
                }
            }
        }
        return result;
    }

    /**
     * List all individuals which are a value of the specified property .
     *
     * @param subjectClassProperty Class contained individuals that are subject
     * of the property
     * @param onProperty property used to select individuals
     * @return list of property values with a instance of the class as subject.
     */
    // @Override
    public List<Individual> listPropertyValuesFromClass(final OntClass subjectClassProperty,
            final ObjectProperty onProperty) {
        final List<Individual> inds = new ArrayList<>();
        // get the individual of Run
        final List<Individual> individuals = this.getIndividuals(subjectClassProperty);
        individuals.stream().map((run) -> run.listPropertyValues(onProperty)).forEachOrdered((listPropertyValues) -> {
            // fill the list of algorithm
            while (listPropertyValues.hasNext()) {
                final Individual next = listPropertyValues.next().as(Individual.class);
                inds.add(next);
            }
        });
        return inds;
    }

    /**
     * List all individuals which are a value of the specified property .
     *
     * @param individual individual that is the subject of the property
     * @param onProperty property used to select individuals
     * @return list of property values with have the individual as subject.
     */
    // @Override
    public List<Individual> listPropertyValues(final Individual individual, final ObjectProperty onProperty) {
        final List<Individual> inds = new ArrayList<>();
        // fill the list of algorithm
        final NodeIterator listPropertyValues = individual.listPropertyValues(onProperty);

        while (listPropertyValues.hasNext()) {
            final Individual next = listPropertyValues.next().as(Individual.class);
            inds.add(next);
        }
        return inds;
    }

    /**
     * Creates a individual of a model.
     *
     * @param name Individual name .
     * @param model Class used to create the individual.
     * @return the individual created.
     */
    public Individual createIndividual(String name, final OntClass model) {
        name = this.getURI(name);
        final Individual indiv = model.createIndividual(name);
        return indiv;
    }

    /**
     * Gets <code>ObjectProperty</code> from the name.
     *
     * @param name name of the property with namespace.
     * @param create <code>true</code> if the ontology create the ObjectProperty
     * if its no exist,<code>false </code>otherwise.
     * @return the <code>ObjectProperty</code> found or null if the property
     * does not exist.
     * @see getDataProperty( String name)
     */
    public ObjectProperty getObjectProperty(final String name, final boolean create) {
        ObjectProperty prprt = this.getObjectProperty(name);
        if (prprt == null && create) {
            prprt = this.createObjectProperty(name);
        }
        return prprt;
    }

    /**
     * Removes all class that do not contained the namespace.
     *
     * @param toList remove list of other class.
     */
    private void cleanOther(final List<OntClass> toList, final String namespace) {
        int i = 0;
        while (i < toList.size()) {
            // get the ontclass
            final OntClass get = toList.get(i);
            // test the class is member of own namespace
            if (get.toString().contains(namespace)) {
                i++;
            } else {
                toList.remove(i);
            }
        }
    }

    /**
     * Test if the property is the subClassOf property
     *
     * @param property property to be tested
     * @return true if the property is subClassOf, false otherwise.
     */
    public boolean isSubClassOf(final Statement property) {
        final String propString = property.toString();
        final boolean contains = propString.contains("subClassOf");
        return contains;
    }

    public boolean isSubclassOf(final Resource outType, final OntClass ontClass) {
        final ExtendedIterator<OntClass> listSuperClasses = this.getOntClass(outType).listSuperClasses();
        for (final Iterator iterator = listSuperClasses; iterator.hasNext();) {
            final OntClass next = (OntClass) iterator.next();
            if (next.equals(ontClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test if the property is created with the specific ontology name space.
     *
     * @param property property to be tested
     * @param namespace namespace to be tested
     * @return true if the property is created with the specific ontology name
     * space, false otherwise.
     */
    public boolean isOwnProperty(final Statement property, final String namespace) {
        final String propString = property.getPredicate().toString();
        final boolean contains = propString.contains(namespace);
        return contains;
    }

    /**
     * Get the object of the restriction property
     *
     * @param restriction restriction contained the object searched
     * @return the object of the restriction or null if the restriction is not
     * yet supported.
     */
    public RDFNode getObjectRestriction(final Restriction restriction) {
        RDFNode res = null;
        if (restriction.isAllValuesFromRestriction()) {
            final AllValuesFromRestriction restrictionsSuported = restriction.asAllValuesFromRestriction();
            res = restrictionsSuported.getAllValuesFrom();
        } else if (restriction.isSomeValuesFromRestriction()) {
            final SomeValuesFromRestriction restrictionsSuported = restriction.asSomeValuesFromRestriction();
            res = restrictionsSuported.getSomeValuesFrom();
        } else if (restriction.isHasValueRestriction()) {
            final HasValueRestriction restrictionsSuported = restriction.asHasValueRestriction();
            final RDFNode rdf = restrictionsSuported.getHasValue();
            res = rdf;
        } else// else if (restriction.isCardinalityRestriction())
        {
            // CardinalityRestriction rest = restriction.asCardinalityRestriction();
            final Statement property = restriction.getProperty(OWL2.onClass);
            if (property != null) {
                final RDFNode object = property.getObject();
                if (object.isResource()) {
                    res = object.asResource();
                }
            }
        }
        return res;
    }

    /**
     * Test if a class has a specific restriction about a property and a object
     *
     * @param subjectClass class that the restriction is researched
     * @param onProperty property researched
     * @param res object value of the property restriction or null if the
     * property has not a specific value restriction.
     * @return true if the class subject has the restriction described, false
     * otherwise.
     */
    public boolean hasRestriction(final OntClass subjectClass, final Property onProperty, final RDFNode res) {
        // gets all super classes
        for (final Iterator<OntClass> i = subjectClass.listSuperClasses(true); i.hasNext();) {
            final OntClass c = i.next();
            if (c.isRestriction()) {
                // get the class as a restriction
                final Restriction r = c.asRestriction();
                final OntProperty iproperty = r.getOnProperty();
                final RDFNode ivalue = this.getObjectRestriction(r);
                boolean isOk = true;
                if (onProperty != null) {
                    isOk &= onProperty.equals(iproperty);
                }
                if (res != null && ivalue.isResource()) {
                    isOk &= res.equals(ivalue);
                }
                if (isOk) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Test if a class has a specific restriction about a property and a object
     *
     * @param subjectClass class that the restriction is researched
     * @param onProperty property researched
     * @return true if the class subject has the restriction described, false
     * otherwise.
     */
    public boolean hasRestriction(final OntClass subjectClass, final Property onProperty) {
        // gets all super classes
        for (final Iterator<OntClass> i = subjectClass.listSuperClasses(true); i.hasNext();) {
            final OntClass c = i.next();
            if (c.isRestriction()) {
                // get the class as a restriction
                final Restriction r = c.asRestriction();
                final OntProperty iproperty = r.getOnProperty();
                if (onProperty != null) {
                    return onProperty.equals(iproperty);
                }
            }
        }
        return false;
    }

    /**
     * Get individual with specific name
     *
     * @param name local name of the individual
     * @return the individual found or null if it not found.
     */
    public Individual getIndividual(String name) {
        name = this.getURI(name);
        return this.ont.getIndividual(name);
    }

    /**
     * Creates individual of the class given with name base on the number of
     * individual contained inside the class.
     *
     * @param ontClass class used to create individual
     * @return individual created
     */
    public Individual createIndividual(final OntClass ontClass) {
        final String localName = this.ns + ontClass.getLocalName() + "_" + UUID.randomUUID().toString();
        return ontClass.createIndividual(localName);
    }

    /**
     * Creates individual of the class given with name base on the number of
     * individual contained inside the class.
     *
     * @param res resource which is the ontclass used to create individual
     * @return individual created
     */
    public Individual createIndividual(final Resource res) {
        return this.createIndividual(this.getOntClass(res));
    }

    /**
     * Add property and value if it does not already exist at every individual
     * of the class that has not yet the property property.
     *
     * @param ontclass class contained individuals.
     * @param onProperty on property added to individuals.
     * @param value value of the property.
     * @param dupliPrprt true if individual can have duplicate property with
     * different object,false otherwise.
     * @return true if the property is added to at least one of the individual,
     * false otherwise.
     * <h3>Example</h3>
     * <p>
     * PiOntology ont = new PiOntology();
     *
     * OntClass ontclass = ont.createClass("testClasse");
     *
     *
     * OntProperty onProperty = ont.createObjectProperty("hasDummy"); Resource
     * object = ont.createClass("classOB"); boolean dupliPrprt = true;
     * ont.addProperty(ontclass, onProperty, object, dupliPrprt);
     * </p>
     */
    public boolean addProperty(final OntClass ontclass, final Property onProperty, final RDFNode value,
            final boolean dupliPrprt) {

        final List<Individual> individuals = this.getIndividuals(ontclass);
        // if no individual are creates in the class
        boolean atLeatOne = false;
        if (individuals.isEmpty()) {
            // creates new individual
            final Individual ind = this.createIndividual(ontclass);
            // add the property and the object value
            ind.addProperty(onProperty, value);
            return true;
        } else {
            for (final Individual individual : individuals) {
                // if the individual has not this property with the same object value
                if (!(dupliPrprt || individual.hasProperty(onProperty))
                        || (dupliPrprt && !individual.hasProperty(onProperty, value))) {
                    // add it
                    individual.addProperty(onProperty, value);
                    atLeatOne = true;
                }
            }
            return atLeatOne;
        }
    }

    /**
     * Creates <code>SomeValuesFromRestriction</code>
     *
     * @param name name of the restriction
     * @param propertyRestriction property on the restriction is applied
     * @param objectRestriction object of the property
     * @return the <code>SomeValuesFromRestriction</code> created.
     */
    public SomeValuesFromRestriction createSomeValuesFromRestriction(final String name,
            final Property propertyRestriction, final Resource objectRestriction) {
        final SomeValuesFromRestriction restriction = this.ont.createSomeValuesFromRestriction(null,
                propertyRestriction, objectRestriction);
        return restriction;
    }
    // --------------------------------------------------------------------------
    // ------------------------------HasValueRestriction-------------------------
    // --------------------------------------------------------------------------

    /**
     * Add <code>AllValuesFromRestriction</code> at given <code>ontClass</code>.
     *
     * @param restricted <code>ontClass</code> restricted
     * @param onProperty property of the restriction
     * @param value value of the restriction property.
     * @see createAllValuesFromRestriction(String uri, Property prop, Resource
     * cls).
     * <h3>Example</h3>
     * <p>
     * PiOntology ont = new PiOntology();
     * </p>
     */
    public void addAllValuesFromRestriction(final OntClass restricted, final Property onProperty,
            final Resource value) {
        final AllValuesFromRestriction createAllValuesFromRestriction = this.createAllValuesFromRestriction(null,
                onProperty, value);
        createAllValuesFromRestriction.addSubClass(restricted);
    }

    // --------------------------------------------------------------------------
    // ------------------------------HasValueRestriction-------------------------
    // --------------------------------------------------------------------------
    /**
     * Add <code>hasValueRestiction</code> at given <code>ontClass</code>.
     *
     * @param restricted <code>ontClass</code> restricted
     * @param onProperty property of the restriction
     * @param value value of the restriction property.
     * @see createHasValueRestriction(Property onProperty, RDFNode value)
     * <h3>Example</h3>
     * <p>
     * PiOntology ont = new PiOntology(); Literal lit =
     * ont.getOnt().createLiteral("Dana"); DatatypeProperty property =
     * ont.getDataProperty("hasName", true);
     * ont.addHasValueRestriction(ont.getOntClass("Dana", true),property, lit);
     * </p>
     */
    public void addHasValueRestriction(final OntClass restricted, final Property onProperty, final RDFNode value) {
        final HasValueRestriction createHasValueRestriction = this.createHasValueRestriction(onProperty, value);
        createHasValueRestriction.addSubClass(restricted);
    }

    /**
     * Creates <code>hasValueRestiction</code> on the property
     * <code>onProperty</code> with value is <code>value</code>
     *
     * @param onProperty property of the restriction
     * @param value value of the restriction property.
     * @return the restriction creates.
     *
     * <h3>Example</h3>
     * <p>
     * PiOntology ont = new PiOntology(); Literal lit =
     * ont.getOnt().createLiteral("Dana"); DatatypeProperty property =
     * ont.getDataProperty("hasName", true); HasValueRestriction
     * createHasValueRestriction = ont.createHasValueRestriction(property, lit);
     * </p>
     */
    public HasValueRestriction createHasValueRestriction(final Property onProperty, final RDFNode value) {
        return this.createHasValueRestriction(null, onProperty, value);
    }

    /**
     * Creates <code>hasValueRestiction</code> on the property
     * <code>onProperty</code> with value is <code>value</code>
     *
     * @param uri URI of the restriction
     * @param onProperty property of the restriction
     * @param value value of the restriction property.
     * @return the restriction creates.
     *
     * <h3>Example</h3>
     * <p>
     * PiOntology ont = new PiOntology(); Literal lit =
     * ont.getOnt().createLiteral("Dana"); DatatypeProperty property =
     * ont.getDataProperty("hasName", true); HasValueRestriction
     * createHasValueRestriction =
     * ont.createHasValueRestriction("rest1",property, lit);
     * </p>
     */
    public HasValueRestriction createHasValueRestriction(final String uri, final Property onProperty,
            final RDFNode value) {
        // if (uri == null) {
        // uri = onProperty.getLocalName() + "_" + value;
        // }
        // HasValueRestriction hasValueRestriction =
        // this.ont.getHasValueRestriction(uri);
        // if (hasValueRestriction == null) {
        final HasValueRestriction hasValueRestriction = this.ont.createHasValueRestriction(uri, onProperty, value);
        // }
        return hasValueRestriction;
    }

    public List<Individual> getIndividuals(final OntClass subject, final ObjectProperty onProperty) {
        final List<Individual> res = new ArrayList<>();
        List<Individual> individuals;
        if (subject != null) {
            individuals = this.ont.listIndividuals(subject).toList();
        } else {
            individuals = this.ont.listIndividuals().toList();
        }
        individuals.stream().filter((ind) -> (ind.hasProperty(onProperty))).forEach((ind) -> {
            res.add(ind);
        }); // test if the individual has the rigth property on the right object.
        return res;
    }

    public RDFNode getObject(final Resource r, final ObjectProperty onProperty) {
        final Statement property = r.getProperty(onProperty);
        final RDFNode object = property.getObject();
        return object;
    }

    public double getDataDouble(final Resource r, final DatatypeProperty onProperty) {
        final Statement property = r.getProperty(onProperty);
        final double object = property.getDouble();
        return object;
    }

    /**
     * Convert resource to individual if it possible
     *
     * @param resource resource to convert in individual.
     * @return the individual converted of null if the conversion it not
     * possible.
     */
    public Individual convert2Individual(final Resource resource) {
        final Individual individual = this.ont.getIndividual(resource.getURI());
        return individual;
    }

    /**
     * Get the OntClass from the resources or null if no OntClass found.
     *
     * @param object resource used to get the OntClass
     * @return the OntClass or null if no OntClass found.
     */
    public OntClass getOntClass(final Resource object) {
        if (object.isURIResource()) {
            return this.getOntClass(object.getURI());
        } else {
            return null;
        }
    }

    /**
     * Adds <code>CardinalityRestriction</code> to the given class
     *
     * @param restricted class to be restricted
     * @param restrictionProperty property of the
     * <code>CardinalityRestriction</code>
     * @param value value for the cardinality.
     * @return the restriction created.
     */
    public CardinalityRestriction addCardinalityRestriction(final OntClass restricted,
            final DatatypeProperty restrictionProperty, final int value) {
        final CardinalityRestriction restriction = this.ont.createCardinalityRestriction(null, restrictionProperty,
                value);
        restriction.addSubClass(restricted);
        return restriction;
    }

    /*
https://github.com/owlcs/ont-api/wiki/Examples
String ns = "https://lab.ponciano.info/pisemantic#";
    OntModel m = OntModelFactory.createModel()
            .setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("q", ns);
    OntDataRange.Named floatDT = m.getDatatype(XSD.xfloat);
    OntFacetRestriction min = m.createFacetRestriction(OntFacetRestriction.MinInclusive.class,
            floatDT.createLiteral("0.0"));
    OntFacetRestriction max = m.createFacetRestriction(OntFacetRestriction.MaxInclusive.class,
            floatDT.createLiteral("15.0"));
    OntDataRange.Named myDT = m.createDatatype(ns + "MyDatatype");
    myDT.addEquivalentClass(m.createDataRestriction(floatDT, min, max));
    m.createResource().addProperty(m.createDataProperty(ns + "someProperty"),
            myDT.createLiteral("2.2"));
    m.write(System.out, "ttl");
     */
}
