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
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import java.io.File;

public class CleanupAction extends BasicAction {
  protected String getActionName(AbstractVcs vcs) {
    return "Cleanup";
  }

  protected boolean needsAllFiles() {
    return false;
  }

  protected boolean isEnabled(Project project, SvnVcs vcs, VirtualFile file) {
    SVNWCClient wcClient = new SVNWCClient(vcs.getSvnAuthenticationManager(), vcs.getSvnOptions());
    try {
      SVNInfo info = wcClient.doInfo(new File(file.getPath()), SVNRevision.WORKING);
      return info != null && info.getKind() == SVNNodeKind.DIR;
    }
    catch (SVNException e) {
      return false;
    }
  }

  protected boolean needsFiles() {
    return false;
  }

  protected void perform(Project project, SvnVcs activeVcs, VirtualFile file, DataContext context, AbstractVcsHelper helper)
    throws VcsException {
    SVNWCClient wcClient = new SVNWCClient(null, null);
    try {
      wcClient.doCleanup(new File(file.getPath()));
    }
    catch (SVNException e) {
      VcsException ve = new VcsException(e);
      ve.setVirtualFile(file);
      throw ve;
    }
  }

  protected void batchPerform(Project project, SvnVcs activeVcs, VirtualFile[] file, DataContext context, AbstractVcsHelper helper)
    throws VcsException {
    throw new VcsException("CleanupAction.batchPerform not implemented");
  }

  protected boolean isBatchAction() {
    return false;
  }
}
