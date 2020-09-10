/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.domain.ApiError;
import de.willuhn.jameica.hbci.transferwise.domain.accounts.Account;
import de.willuhn.jameica.hbci.transferwise.domain.accounts.Balance;
import de.willuhn.jameica.hbci.transferwise.domain.accounts.BankDetails;
import de.willuhn.jameica.hbci.transferwise.domain.profiles.UserProfile;
import de.willuhn.jameica.hbci.transferwise.gui.dialogs.ProfileSelectDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Uebernimmt die Datenuebertragung.
 */
@Lifecycle(Type.CONTEXT)
public class TransportService
{
  private final static Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  private CloseableHttpClient client = null;
  private final ObjectMapper mapper = new ObjectMapper();

  
  /**
   * Initialisiert den Service.
   */
  @PostConstruct
  private void init()
  {
    Logger.info("init transferwise transport service");
    this.client = HttpClients.createDefault();
  }
  
  /**
   * Beendet den Service.
   */
  @PreDestroy
  private void shutdown()
  {
    Logger.info("shutting down transferwise transport service");
    try
    {
      IOUtil.close(this.client);
    }
    finally
    {
      this.client = null;
    }
  }
  
  /**
   * Liefert das zu verwendende Profil.
   * @param konto
   * @return das zu verwendende Profil.
   * @throws ApplicationException
   */
  public String getProfile(Konto konto) throws ApplicationException
  {
    try
    {
      // Checken, ob der User fuer das Konto schon ein Profil ausgewaehlt hat.
      String profile = konto.getMeta(Plugin.META_PARAM_PROFILE,null);
      if (StringUtils.trimToNull(profile) != null)
        return profile;
      
      Logger.info("fetching profiles");
      UserProfile[] result = this.get(konto,"/v1/profiles",null,UserProfile[].class);

      if (result == null || result.length == 0)
      {
        Logger.warn("no profiles found, not sure if this will work");
        return null;
      }
      
      try
      {
        if (result.length == 1 && result[0].id != null)
        {
          profile = result[0].id.toString();
          Logger.info("auto-choosing profile id " + profile);
          return profile;
        }
        
        if (Application.inServerMode())
        {
          for (UserProfile p:result)
          {
            if (p.id != null)
            {
              profile = p.id.toString();
              Logger.info("running in server-mode, unable to ask for user profile to be used, selecting first profile " + profile);
              return profile;
            }
          }
        }
        else
        {
          // User fragen
          Logger.info("asking user for profile to be used");
          ProfileSelectDialog d = new ProfileSelectDialog(Arrays.asList(result),ProfileSelectDialog.POSITION_CENTER);
          UserProfile p = d.open();
          if (p != null && p.id != null)
          {
            profile = p.id.toString();
            Logger.info("chosen profile id " + profile);
            return profile;
          }
        }
        Logger.warn("unable to determine user profile");
        return null;
      }
      finally
      {
        if (profile != null)
        {
          Logger.info("saving profile id " + profile + " for account id " + konto.getID());
          konto.setMeta(Plugin.META_PARAM_PROFILE,profile);
        }
      }

    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error",e);
      throw new ApplicationException(i18n.tr("Fehler beim Abrufen der Benutzer-Profile"),e);
    }
  }
  
  /**
   * Liefert den zu verwendenden Account.
   * @param konto
   * @return der zu verwendende Account.
   * @throws ApplicationException
   */
  public String getAccount(Konto konto) throws ApplicationException
  {
    try
    {
      // Checken, ob der User fuer das Konto schon ein Account ausgewaehlt hat.
      String account = konto.getMeta(Plugin.META_PARAM_ACCOUNT,null);
      if (StringUtils.trimToNull(account) != null)
        return account;
      
      Logger.info("fetching accounts");
      Map<String,String> params = new HashMap<String,String>();
      params.put("profileId",this.getProfile(konto));
      Account[] result = this.get(konto,"/v1/borderless-accounts",params,Account[].class);

      if (result == null || result.length == 0)
      {
        Logger.warn("no accounts found, not sure if this will work");
        return null;
      }
      
      final String iban = StringUtils.trimToEmpty(konto.getIban()).replace(" ","");
      
      try
      {
        // Wir iterieren ueber die Konten und nehmen das erste, bei dem die IBAN passt
        for (Account a:result)
        {
          if (a == null || a.id == null)
            continue;
          
          List<Balance> balances = a.balances;
          if (balances == null || balances.size() == 0)
            continue;
          
          for (Balance b:balances)
          {
            BankDetails detail = b.bankDetails;
            if (detail == null)
              continue;

            String test = StringUtils.trimToNull(detail.iban);
            if (test == null)
              test = StringUtils.trimToNull(detail.accountNumber); // Fallback
            
            if (test == null)
              continue;
            
            test = test.replace(" ","");
            if (test.equalsIgnoreCase(iban))
            {
              account = a.id.toString();
              return account;
            }
          }
        }
        Logger.warn("unable to determine account");
        return null;
      }
      finally
      {
        if (account != null)
        {
          Logger.info("saving account id " + account + " for konto id " + konto.getID());
          konto.setMeta(Plugin.META_PARAM_ACCOUNT,account);
        }
      }
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error",e);
      throw new ApplicationException(i18n.tr("Fehler beim Abrufen der Benutzer-Profile"),e);
    }
  }

  /**
   * Fuehrt einen GET-Request aus.
   * @param <T> der Response-Typ.
   * @param konto das Konto.
   * @param path der Pfad.
   * @param params die Parameter.
   * @param type der Response-Typ.
   * @return die deserialisierten Antwort-Daten.
   * @throws ApplicationException 
   */
  public <T> T get(Konto konto, String path, Map<String,String> params, Class<T> type) throws ApplicationException
  {
    CloseableHttpResponse response = null;
    
    try
    {
      final String apiKey = konto.getMeta(Plugin.META_PARAM_APIKEY,null);
      if (StringUtils.trimToNull(apiKey) == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen API-Key in den Synchronisationsoptionen ein."));
      
      final String cust = konto.getKundennummer();
      if (StringUtils.trimToNull(cust) == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Kundenkennung ein."));

      final URIBuilder b = new URIBuilder();
      b.setScheme("https");
      b.setHost(this.getApiEndpoint().getHostname());
      b.setPath((path.startsWith("/") ? "" : "/") + path);
      if (params != null)
      {
        for (Entry<String,String> e:params.entrySet())
        {
          b.addParameter(e.getKey(),e.getValue());
        }
      }
      
      URI uri = b.build();
      Logger.info("executing GET: " + uri);
      final HttpGet request = new HttpGet(uri);
      request.addHeader("Authorization","Bearer " + apiKey);
      
      response = this.client.execute(request);
      final StatusLine status = response.getStatusLine();
      final String json = this.read(response.getEntity().getContent());
      if (status.getStatusCode() > 299)
      {
        String msg = status.getStatusCode() + " " + status.getReasonPhrase();
        Logger.error("got http status  " + msg);
        
        // Checken, ob wir den Fehler lesen koennen
        try
        {
          ApiError error = this.mapper.readValue(json, ApiError.class);
          msg = error.error + ": " + error.message;
        }
        catch (Exception e)
        {
        }
        throw new ApplicationException(msg);
      }
      return this.mapper.readValue(json, type);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("unable to create GET request",e);
      throw new ApplicationException(i18n.tr("Fehler beim Erstellen der Abfrage: {0}",e.getMessage()));
    }
    finally
    {
      IOUtil.close(response);
    }
  }
  
  /**
   * Liest die JSON-Daten aus dem Stream und loggt sie.
   * @param is der Stream.
   * @return die gelesenen Daten.
   * @throws IOException
   */
  private String read(InputStream is) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtil.copy(is,bos);
    final String s = bos.toString("UTF-8");
    Logger.info("response: " + s);
    return s;
  }
  
  /**
   * Liefert den aktuell konfigurierten API-Endpunkt.
   * @return der aktuell konfigurierte API-Endpunkt.
   */
  private ApiEndpoint getApiEndpoint()
  {
    return ApiEndpoint.valueOf(settings.getString("andpoint",ApiEndpoint.LIVE.name()));
  }
  
}


