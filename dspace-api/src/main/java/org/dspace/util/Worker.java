/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.util;

import java.util.Map;

/**
 * Represent a background task to be run with arguments.
 *
 * @author mwood
 */
public interface Worker
{
    public void work(Map<String, String> args);
}
