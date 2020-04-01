/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.willuhn.jameica.hbci.transferwise.Plugin;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ApiError
{
  /**
   * Zeitstempel.
   */
  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=Plugin.DATEFORMAT, timezone=Plugin.TIMEZONE)
  public Date timestamp;
  
  /**
   * HTTP-Statuscode.
   */
  public Integer status;

  /**
   * Fehlertext.
   */
  public String error;
  
  /**
   * Fehlerbeschreibung.
   */
  public String message;
  
  /**
   * Exception.
   */
  public String exception;
  
  /**
   * Pfad.
   */
  public String path;
  
}


