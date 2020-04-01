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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Backend, welches Konten via Transferwise anbindet.
 */
@Lifecycle(Type.CONTEXT)
public class TransferwiseSynchronizeBackend extends AbstractSynchronizeBackend<TransferwiseSynchronizeJobProvider>
{
  @Resource
  private SynchronizeEngine engine = null;

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeBackend#getName()
   */
  public String getName()
  {
    return "Transferwise";
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#getJobProviderInterface()
   */
  protected Class<TransferwiseSynchronizeJobProvider> getJobProviderInterface()
  {
    return TransferwiseSynchronizeJobProvider.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#createJobGroup(de.willuhn.jameica.hbci.rmi.Konto)
   */
  protected de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend.JobGroup createJobGroup(Konto k)
  {
    return new TransferwiseJobGroup(k);
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#getSynchronizeKonten(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public List<Konto> getSynchronizeKonten(Konto k)
  {
    List<Konto> list = super.getSynchronizeKonten(k);
    List<Konto> result = new ArrayList<Konto>();
    
    // Wir wollen nur die Offline-Konten und jene, bei denen Scripting explizit konfiguriert ist
    for (Konto konto:list)
    {
      if (this.supports(konto))
        result.add(konto);
    }
    
    return result;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#create(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   */
  public <T> T create(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException
  {
    if (!this.supports(type,konto))
      throw new ApplicationException(i18n.tr("Der Geschäftsvorfall wird nicht unterstützt"));
    
    return(T) super.create(type,konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#supports(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   */
  public boolean supports(Class<? extends SynchronizeJob> type, Konto konto)
  {
    if (!this.supports(konto))
      return false;

    return super.supports(type,konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#execute(java.util.List)
   */
  public synchronized SynchronizeSession execute(List<SynchronizeJob> jobs) throws ApplicationException, OperationCanceledException
  {
    try
    {
      // Wir checken extra noch, ob es wirklich alles Offline-Konten sind oder ob bei denen das Scripting ausgewaehlt wurde
      for (SynchronizeJob job:jobs)
      {
        Konto konto = job.getKonto();
        if (!this.supports(konto))
          throw new ApplicationException(i18n.tr("Das Zugangsverfahren {0} unterstützt das Konto {1} nicht",this.getName(),konto.getLongName()));
      }
    }
    catch (RemoteException re)
    {
      Logger.error("error while performing synchronization",re);
      throw new ApplicationException(i18n.tr("Synchronisierung fehlgeschlagen: {0}",re.getMessage()));
    }

    return super.execute(jobs);
  }
  
  /**
   * Prueft, ob das Konto prinzipiell unterstuetzt wird.
   * @param konto das Konto.
   * @return true, wenn es prinzipiell unterstuetzt wird.
   */
  boolean supports(Konto konto)
  {
    if (konto == null)
      return false;
    
    try
    {
      if (konto.hasFlag(Konto.FLAG_DISABLED))
        return false;
      
      String backend = StringUtils.trimToNull(konto.getBackendClass());
      if (!Objects.equals(backend,this.getClass().getName()))
        return false;
      
      // Checken, ob die BIC passt.
      return Objects.equals(StringUtils.trimToNull(konto.getBic()),Plugin.BIC_TRANSFERWISE);
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine synchronization support for konto",re);
    }
    return false;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend#getPropertyNames(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public List<String> getPropertyNames(Konto konto)
  {
    if (!this.supports(konto))
      return null;
    
    return Arrays.asList(Plugin.META_PARAM_APIKEY);
  }

  /**
   * Unsere Implementierung.
   */
  protected class TransferwiseJobGroup extends JobGroup
  {
    /**
     * ct.
     * @param k das Konto.
     */
    protected TransferwiseJobGroup(Konto k)
    {
      super(k);
    }

    /**
     * @see de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend.JobGroup#sync()
     */
    protected void sync() throws Exception
    {
      ////////////////////////////////////////////////////////////////////
      // lokale Variablen
      ProgressMonitor monitor = worker.getMonitor();
      
      double chunk  = 100d / (worker.getSynchronization().size()) * (this.jobs.size());
      double window = chunk - 6d;
      getCurrentSession().setProgressWindow(window);
      ////////////////////////////////////////////////////////////////////

      this.checkInterrupted();

      monitor.log(" ");
      monitor.log(i18n.tr("Synchronisiere Konto: {0}",this.getKonto().getLongName()));

      Logger.info("processing jobs");
      for (SynchronizeJob job:this.jobs)
      {
        this.checkInterrupted();
        monitor.setStatusText(i18n.tr("Führe Geschäftsvorfall aus: \"{0}\"",job.getName()));
        ((TransferwiseSynchronizeJob)job).exeute();
      }
    }
  }
}
