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
