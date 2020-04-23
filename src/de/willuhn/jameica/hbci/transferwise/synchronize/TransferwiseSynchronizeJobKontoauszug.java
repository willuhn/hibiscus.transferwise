/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.synchronize;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.domain.accountstatements.AccountStatement;
import de.willuhn.jameica.hbci.transferwise.domain.accountstatements.Amount;
import de.willuhn.jameica.hbci.transferwise.domain.accountstatements.Transaction;
import de.willuhn.jameica.hbci.transferwise.domain.accountstatements.TransactionDetails;
import de.willuhn.jameica.hbci.transferwise.transport.TransportService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Jobs fuer den Abruf der Kontoauszuege per Transferwise.
 */
public class TransferwiseSynchronizeJobKontoauszug extends SynchronizeJobKontoauszug implements TransferwiseSynchronizeJob
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  
  @Resource private TransportService transportService;

  /**
   * @see de.willuhn.jameica.hbci.transferwise.synchronize.TransferwiseSynchronizeJob#exeute()
   */
  @Override
  public void exeute() throws ApplicationException
  {
    
    try
    {
      final Konto k = (Konto) this.getContext(CTX_ENTITY);
      final String profile = this.transportService.getProfile(k);
      final String account = this.transportService.getAccount(k);
      
      final StringBuilder sb = new StringBuilder("/v3/profiles/").append(profile);
      sb.append("/borderless-accounts/").append(account);
      sb.append("/statement.json");

      final DateFormat df = new SimpleDateFormat(Plugin.DATEFORMAT);
      final Date startDate = this.getStartDate(k);

      final Map<String,String> params = new HashMap<String,String>();
      final String curr = StringUtils.trimToNull(k.getWaehrung());
      params.put("currency",curr != null ? curr : HBCIProperties.CURRENCY_DEFAULT_DE);
      params.put("intervalStart",df.format(startDate));
      params.put("intervalEnd",df.format(DateUtil.endOfDay(new Date())));
      
      final AccountStatement as = this.transportService.get(k,sb.toString(),params,AccountStatement.class);
      
      if (as != null)
      {
        int created = 0;
        int skipped = 0;
        
        if (as.transactions != null && as.transactions.size() > 0)
        {
          final DBIterator existing = k.getUmsaetze(this.getMergeWindow(startDate,as),null);
          
          Logger.info("applying entries");
          
          for (Transaction t:as.transactions)
          {
            final Umsatz umsatz = convert(t);
            umsatz.setKonto(k);

            boolean found = false;
            
            /////////////////////////////////////////
            // Checken, ob wir den Umsatz schon haben
            existing.begin();
            for (int i = 0; i<existing.size(); i++)
            {
              GenericObject dbObject = existing.next();
              found = dbObject.equals(umsatz);
              if (found)
              {
                skipped++; // Haben wir schon
                break;
              }
            }
              /////////////////////////////////////////
            
            // Umsatz neu anlegen
            if (!found)
            {
              try
              {
                umsatz.store(); // den Umsatz haben wir noch nicht, speichern!
                Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
                created++;
              }
              catch (Exception e2)
              {
                Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Nicht alle empfangenen Umsätze konnten gespeichert werden. Bitte prüfen Sie das System-Protokoll"),StatusBarMessage.TYPE_ERROR));
                Logger.error("error while adding umsatz, skipping this one",e2);
              }
            }
          }
        }
        
        k.addToProtokoll(i18n.tr("Umsätze abgerufen"),Protokoll.TYP_SUCCESS);
        Amount saldo = as.endOfStatementBalance;
        if (saldo != null && saldo.value != null)
        {
          k.setSaldo(saldo.value.doubleValue());
          k.store();
          Application.getMessagingFactory().sendMessage(new SaldoMessage(k));
        }
        
        Logger.info("done. new entries: " + created + ", skipped entries (already in database): " + skipped);
      }
      else
      {
        Logger.info("got no new entries");
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
      throw new ApplicationException(i18n.tr("Fehler beim Abrufen der Kontoauszüge"),e);
    }
  }
  
  /**
   * Liefert das Startdatum fuer den Abgleich mit den existierenden Umsaetzen.
   * @param startDate das von uns gesendete Startdatum. Wird in {@link AccountStatement} zwar auch nochmal zurueckgeliefert.
   * Aber warum auf die Daten in der Antwort verlassen, wenn wir das Datum selbst kennen.
   * @param as die Liste der Umsatzbuchungen.
   * @return das Startdatum. Kann NULL sein.
   */
  private Date getMergeWindow(final Date startDate, final AccountStatement as)
  {
    Date d = null;
    String basedOn = null;
    
    for (Transaction ts:as.transactions)
    {
      Date nd = ts.date;
      if (d == null || nd.before(d))
        d = nd;
    }
    
    if (d == null && startDate != null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(startDate);
      cal.add(Calendar.DATE,settings.getInt("umsatz.mergewindow.offset",-30));
      d = cal.getTime();
      basedOn = "last sync";
    }
    
    if (d == null)
      Logger.info("merge window: not set");
    else
      Logger.info("merge window: " + d + " - now (based on " + basedOn + ")");
    
    return d;
  }
  
  /**
   * Konvertiert die Buchung in einen Hibiscus-Datensatz.
   * @param t die Buchung.
   * @return der Hibiscus-Datensatz.
   * @throws Exception
   */
  private Umsatz convert(Transaction t) throws Exception
  {
    Umsatz umsatz = (Umsatz) de.willuhn.jameica.hbci.Settings.getDBService().createObject(Umsatz.class,null);
    umsatz.setTransactionId(t.referenceNumber);

    TransactionDetails td = t.details;
    
    if (td.type != null)
      umsatz.setArt(clean(td.type.name()));
    
    Amount saldo = t.runningBalance;
    if (saldo != null && saldo.value != null)
      umsatz.setSaldo(saldo.value.doubleValue());

    Amount value = t.amount;
    if (value == null)
      value = td.amount;
    if (value == null)
      value = td.targetAmount;

    if (value != null)
      umsatz.setBetrag(value.value.doubleValue());
    
    umsatz.setDatum(t.date);
    umsatz.setValuta(t.date);

    if (td.description != null)
      VerwendungszweckUtil.applyCamt(umsatz,Arrays.asList(td.description));

    ////////////////////////////////////////////////////////////////////////////
    // Gegenkonto
    if (td.senderAccount != null || td.senderName != null)
    {
      HibiscusAddress e = (HibiscusAddress) de.willuhn.jameica.hbci.Settings.getDBService().createObject(HibiscusAddress.class,null);
      e.setIban(td.senderAccount);

      String name = td.senderName;
      if (name != null && name.length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
        name = StringUtils.trimToEmpty(name.substring(0,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH));
      e.setName(name);
      umsatz.setGegenkonto(e);
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    return umsatz;
  }
  
  /**
   * Entfernt Zeichen, die in den Strings nicht enthalten sein sollten.
   * Typischerweise Zeilenumbrueche.
   * @param s der String.
   * @return der bereinigte String.
   */
  private static String clean(String s)
  {
    return HBCIProperties.replace(s,HBCIProperties.TEXT_REPLACEMENTS_UMSATZ);
  }

  /**
   * Liefert das zu verwendende Saldo-Datum.
   * @param k das Konto.
   * @return das Saldo-Datum.
   */
  private Date getStartDate(Konto k) throws RemoteException
  {
    Date start = k.getSaldoDatum();
    if (start != null)
    {
      // Checken, ob das Datum vielleicht in der Zukunft liegt. Das ist nicht zulaessig
      Date now = new Date();
      if (start.after(now))
      {
        Logger.warn("future start date " + start + " given. this is not allowed, changing to current date " + now);
        start = now;
      }
    }
    else
    {
      // Wir nehmen das letzte Jahr
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.YEAR,-1);
      start = cal.getTime();
    }
    start = DateUtil.startOfDay(start);
    Logger.info("startdate: " + HBCI.LONGDATEFORMAT.format(start));
    return start;
  }
}
