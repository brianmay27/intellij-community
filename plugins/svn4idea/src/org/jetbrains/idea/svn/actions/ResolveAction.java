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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ResolveAction extends BasicAction {
  protected String getActionName(AbstractVcs vcs) {
    return "Resolve Conflict";
  }

  protected boolean needsAllFiles() {
    return true;
  }

  protected boolean isEnabled(Project project, SvnVcs vcs, VirtualFile file) {
    SVNStatusClient stClient = new SVNStatusClient(null, null);
    SVNWCClient wcClient = new SVNWCClient(null, null);
    try {
      SVNStatus status = stClient.doStatus(new File(file.getPath()), false);
      if (status != null && status.getContentsStatus() == SVNStatusType.STATUS_CONFLICTED) {
        SVNInfo info = wcClient.doInfo(new File(file.getPath()), SVNRevision.WORKING);
        return info != null && info.getConflictNewFile() != null &&
               info.getConflictOldFile() != null &&
               info.getConflictWrkFile() != null;
      }
    }
    catch (SVNException e) {
      //
    }
    return false;
  }

  protected boolean needsFiles() {
    return true;
  }

  protected void perform(Project project, SvnVcs activeVcs, VirtualFile file, DataContext context, AbstractVcsHelper helper)
    throws VcsException {
    batchPerform(project, activeVcs, new VirtualFile[]{file}, context, helper);
  }

  protected void batchPerform(Project project, SvnVcs activeVcs, VirtualFile[] file, DataContext context, AbstractVcsHelper helper)
    throws VcsException {
    List<VirtualFile> files = Arrays.asList(file);
    MergeAction action = new MergeAction(files, project);
    action.execute(null);
  }

  protected boolean isBatchAction() {
    return true;
  }
}
