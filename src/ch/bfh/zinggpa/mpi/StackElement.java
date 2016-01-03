/*
 * Copyright (c) 2015 Berner Fachhochschule, Switzerland.
 *
 * Project Smart Reservation System.
 *
 * Distributable under GPL license. See terms of license at gnu.org.
 */
package ch.bfh.zinggpa.mpi;

import java.util.LinkedList;

public class StackElement {

    private Thread.State parent; // current node on the search-tree  
    private LinkedList unexpanded; //the unexpanded children of 'parent'


}
