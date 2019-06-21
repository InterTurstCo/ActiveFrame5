package ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckResult;
import ru.intertrust.cm.core.gui.model.plugin.CollectionCheckerPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Iterator;

public class ProcessingEngine {
  private static Boolean checkStarted = false;

  private static final String MSG_OK = "...OK";
  private static final String MSG_STARTED = "Started ...";
  private static final String MSG_PROCESSING = "Processing ";
  private static final String MSG_COMPLETED = "Completed.";
  private static final String MSG_STOPPED = "Stopped by user ... ";
  private static final String MSG_ERROR = "...Error";
  private static Iterator<CollectionConfig> iterator;
  private static CollectionCheckerPluginView pView;
  private static CollectionCheckerPluginData pData;
  private static Long successProcessed = 0L;
  private static Long errorProcessed = 0L;

  public static void start(CollectionCheckerPluginView view, CollectionCheckerPluginData data) {
    if (!checkStarted) {
      pView = view;
      pData = data;
      checkStarted = true;
      view.clearConsole();
      view.putMessage(MSG_STARTED);
      if (pData.getCollections().size() > 0) {
        iterator = pData.getCollections().iterator();
        iterate();
      }
    }
  }

  public static void stop(CollectionCheckerPluginView view) {
    if (checkStarted) {
      checkStarted = false;
      view.putMessage(MSG_STOPPED);
      successProcessed = 0L;
      errorProcessed = 0L;
    }
  }

  private static void iterate() {
    if (iterator.hasNext() && checkStarted) {
      check(iterator.next());
    } else {
      pView.putMessage(MSG_COMPLETED+" Collections amount: "
          +pData.getCollectionsCount()+" successfully processed "+successProcessed+" Processed with errors "+errorProcessed);
      checkStarted=false;
      successProcessed = 0L;
      errorProcessed = 0L;
    }
  }

  private static void check(CollectionConfig config) {
    pView.addMessage(MSG_PROCESSING + config.getName());
    final CollectionCheckResult requestData = new CollectionCheckResult();
    requestData.setConfig(config);

    Command command = new Command("checkCollection", "configuration.check.collection.plugin", requestData);
    BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
      @Override
      public void onFailure(Throwable caught) {
        GWT.log("something was going wrong while obtaining statistics");
        caught.printStackTrace();
        pView.putMessage(MSG_ERROR);
        pView.putMessage(caught.getMessage());
        errorProcessed++;
        iterate();
      }

      @Override
      public void onSuccess(Dto result) {
        if (((CollectionCheckResult) result).getSuccess()) {
          pView.putMessage(MSG_OK);
          successProcessed++;
          iterate();
        } else {
          pView.putMessage(MSG_ERROR);
          pView.putMessage(((CollectionCheckResult) result).getException());
          errorProcessed++;
          iterate();
        }
      }
    });
  }
}
