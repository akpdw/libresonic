/*
 This file is part of Libresonic.

 Libresonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Libresonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Libresonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2017 (C) Libresonic Authors
 Based upon Subsonic, Copyright 2009 (C) Sindre Mehus
 */
package org.libresonic.player.service.upnp;

import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.libresonic.player.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Allen Petersen
 * @version $Id$
 */
public abstract class UpnpContentProcessor<T extends Object, U extends Object> {

  @Autowired
  private DispatchingContentDirectory dispatchingContentDirectory;

  protected String rootTitle;
  protected String rootId;

  /**
   * Browses the root metadata for a type.
   */
  public BrowseResult browseRootMetadata() throws Exception {
    DIDLContent didl = new DIDLContent();
    didl.addContainer(createRootContainer());
    return createBrowseResult(didl, 1, 1);
  }

  public Container createRootContainer() throws Exception {
    Container container = new StorageFolder();
    container.setId(getRootId());
    container.setTitle(getRootTitle());

    int childCount = getAllItemsSize();
    container.setChildCount(childCount);
    container.setParentID(DispatchingContentDirectory.CONTAINER_ID_ROOT);
    return container;
  }

  /**
   * Browses the top-level content of a type.
   */
  public BrowseResult browseRoot(String filter, long firstResult, long maxResults, SortCriterion[] orderBy) throws Exception {
    DIDLContent didl = new DIDLContent();
    List<T> allItems = getAllItems();
    if (filter != null) {
      // filter items (not implemented yet)
    }
    if (orderBy != null) {
      // sort items (not implemented yet)
    }
    List<T> selectedItems = Util.subList(allItems, firstResult, maxResults);
    for (T item : selectedItems) {
      didl.addContainer(createContainer(item));
    }

    return createBrowseResult(didl, (int) didl.getCount(), allItems.size());
  }

  /**
   * Browses metadata for a child.
   */
  public BrowseResult browseObjectMetadata(String id) throws Exception {
    T item = getItemById(id);
    DIDLContent didl = new DIDLContent();
    didl.addContainer(createContainer(item));
    return createBrowseResult(didl, 1, 1);
  }

  /**
   * Browses a child of the container.
   */
  public BrowseResult browseObject(String id, String filter, long firstResult, long maxResults, SortCriterion[] orderBy) throws Exception {
    T item = getItemById(id);
    List<U> allChildren = getChildren(item);
    if (filter != null) {
      // filter items (not implemented yet)
    }
    if (orderBy != null) {
      // sort items (not implemented yet)
    }
    List<U> selectedChildren = Util.subList(allChildren, firstResult, maxResults);

    DIDLContent didl = new DIDLContent();
    for (U child : selectedChildren) {
      addChild(didl, child);
    }
    return createBrowseResult(didl, selectedChildren.size(), allChildren.size());
  }

  protected BrowseResult createBrowseResult(DIDLContent didl, int count, int totalMatches) throws Exception {
    return new BrowseResult(new DIDLParser().generate(didl), count, totalMatches);
  }

  public BrowseResult searchByName(String name,
                                   long firstResult, long maxResults,
                                   SortCriterion[] orderBy)
    throws ContentDirectoryException {
    // default implementation; should be overridden by subclasses
    // that support search
    return null;
   }

  public DispatchingContentDirectory getDispatchingContentDirectory() {
    return dispatchingContentDirectory;
  }
  public void setDispatchingContentDirectory(DispatchingContentDirectory dispatchingContentDirectory) {
    this.dispatchingContentDirectory = dispatchingContentDirectory;
  }
  public DispatchingContentDirectory getDispatcher() {
    return getDispatchingContentDirectory();
  }

  public abstract Container createContainer(T item) throws Exception;

  public abstract List<T> getAllItems() throws Exception;

  // this can probably be optimized in some cases
  public int getAllItemsSize() throws Exception {
    return getAllItems().size();
  }

  public abstract T getItemById(String id) throws Exception;

  public abstract List<U> getChildren(T item) throws Exception;

  public abstract void addChild(DIDLContent didl, U child) throws Exception;

  public String getRootTitle() {
    return rootTitle;
  }
  public void setRootTitle(String rootTitle) {
    this.rootTitle = rootTitle;
  }
  public String getRootId() {
    return rootId;
  }
  public void setRootId(String rootId) {
    this.rootId = rootId;
  }
}

