/**
 * @copyright
 * ====================================================================
 * Copyright (c) 2003-2004 QintSoft.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://subversion.tigris.org/license-1.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 *
 * This software consists of voluntary contributions made by many
 * individuals.  For exact contribution history, see the revision
 * history and logs, available at http://svnup.tigris.org/.
 * ====================================================================
 * @endcopyright
 */
package org.jetbrains.idea.svn.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.dialogs.CopyDialog;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import java.io.File;

public class CopyAction extends BasicAction {
  protected String getActionName(AbstractVcs vcs) {
    return "Branch or Tag...";
  }

  protected boolean needsAllFiles() {
    return true;
  }

  protected boolean isEnabled(Project project, SvnVcs vcs, VirtualFile file) {
    if (file == null) {
      return false;
    }
    try {
      SVNWCClient wcClient = vcs.createWCClient();
      SVNInfo info = wcClient.doInfo(new File(file.getPath()), SVNRevision.WORKING);
      return info != null && info.getURL() != null;
    }
    catch (SVNException e) {
      // ok
    }
    return false;
  }

  protected boolean needsFiles() {
    return true;
  }

  protected void perform(final Project project, final SvnVcs activeVcs, VirtualFile file, DataContext context, AbstractVcsHelper helper)
    throws VcsException {
    final File srcFile = new File(file.getPath());
    CopyDialog dialog = new CopyDialog(project, true, srcFile);
    dialog.show();
    if (dialog.isOK()) {
      final String dstURL = dialog.getToURL();
      final SVNRevision revision = dialog.getRevision();
      final String comment = dialog.getComment();
      final SVNException[] exception = new SVNException[1];

      Runnable copyCommand = new Runnable() {
        public void run() {
          try {
            ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
            SVNCopyClient client = activeVcs.createCopyClient();
            if (progress != null) {
              progress.setText("Copy to '" + dstURL + "'");
              client.setEventHandler(new CopyEventHandler(progress));
            }
            System.out.println("copy from revision: " + revision);
            SVNCommitInfo result = client.doCopy(srcFile, SVNRevision.UNDEFINED, revision, dstURL, SVNRevision.HEAD, false, comment);
            if (result != null && result != SVNCommitInfo.NULL) {
              WindowManager.getInstance().getStatusBar(project).setInfo("Comitted revision " + result.getNewRevision() + ".");
            }
          }
          catch (SVNException e) {
            exception[0] = e;
          }
        }
      };
      ApplicationManager.getApplication().runProcessWithProgressSynchronously(copyCommand, "Subversion Copy", false, project);
      if (exception[0] != null) {
        throw new VcsException(exception[0]);
      }
    }
  }

  protected void batchPerform(Project project, SvnVcs activeVcs, VirtualFile[] files, DataContext context, AbstractVcsHelper helper)
    throws VcsException {
  }

  protected boolean isBatchAction() {
    return false;
  }

  private static class CopyEventHandler implements ISVNEventHandler {
    private ProgressIndicator myProgress;

    public CopyEventHandler(ProgressIndicator progress) {
      myProgress = progress;
    }

    public void handleEvent(SVNEvent event, double p) {
      String path = event.getFile() != null ? event.getFile().getName() : event.getPath();
      if (path == null) {
        return;
      }
      if (event.getAction() == SVNEventAction.COMMIT_ADDED) {
        myProgress.setText2("Adding '" + path + "'");
      }
      else if (event.getAction() == SVNEventAction.COMMIT_DELETED) {
        myProgress.setText2("Deleting '" + path + "'");
      }
      else if (event.getAction() == SVNEventAction.COMMIT_MODIFIED) {
        myProgress.setText2("Sending '" + path + "'");
      }
      else if (event.getAction() == SVNEventAction.COMMIT_REPLACED) {
        myProgress.setText2("Replacing '" + path + "'");
      }
      else if (event.getAction() == SVNEventAction.COMMIT_DELTA_SENT) {
        myProgress.setText2("Transmitting delta for  '" + path + "'");
      }
    }

    public void checkCancelled() {
    }
  }
}
