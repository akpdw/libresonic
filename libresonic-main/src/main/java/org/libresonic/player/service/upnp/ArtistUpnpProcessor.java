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
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicArtist;
import org.libresonic.player.dao.ArtistDao;
import org.libresonic.player.domain.*;
import org.libresonic.player.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Allen Petersen
 * @version $Id$
 */
public class ArtistUpnpProcessor extends UpnpContentProcessor <Artist, Album> {

  @Autowired
  private ArtistDao artistDao;

  public ArtistUpnpProcessor() {
    setRootId(DispatchingContentDirectory.CONTAINER_ID_ARTIST_PREFIX);
    setRootTitle("Artists");
  }

  public BrowseResult searchByName(String name,
                                   long firstResult, long maxResults,
                                   SortCriterion[] orderBy)
    throws ContentDirectoryException {

    DIDLContent didl = new DIDLContent();

    List<Artist> allArtists = getAllItems();
    List<Artist> matches = new ArrayList<Artist>();
    Pattern searchPattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
    for (Artist a: allArtists) {
      Matcher matcher = searchPattern.matcher(a.getName());
      if (matcher.find()){
        matches.add(a);
      }
    }

    List<Artist> selectedItems = Util.subList(matches, firstResult, maxResults);
    for (Artist item : selectedItems) {
      didl.addContainer(createContainer(item));
    }

    try {
      return createBrowseResult(didl, (int) didl.getCount(), matches.size());
    } catch (Exception e) {
      return null;
    }
  }

  public Container createContainer(Artist artist) {
    MusicArtist container = new MusicArtist();
    container.setId(getRootId() + DispatchingContentDirectory.SEPARATOR + artist.getId());
    container.setParentID(getRootId());
    container.setTitle(artist.getName());
    container.setChildCount(artist.getAlbumCount());

    return container;
  }

  public List<Artist> getAllItems() {
    List<MusicFolder> allFolders = getDispatcher().getSettingsService().getAllMusicFolders();
    List<Artist> allArtists = getArtistDao().getAlphabetialArtists(0, Integer.MAX_VALUE, allFolders);
    // alpha artists doesn't quite work :P
    allArtists.sort((Artist o1, Artist o2)->o1.getName().replaceAll("\\W", "").compareToIgnoreCase(o2.getName().replaceAll("\\W", "")));

    return allArtists;
  }

  public Artist getItemById(String id) throws Exception {
    return getArtistDao().getArtist(Integer.parseInt(id));
  }

  public  List<Album> getChildren(Artist artist) {
    List<MusicFolder> allFolders = getDispatcher().getSettingsService().getAllMusicFolders();
    List<Album> allAlbums = getAlbumProcessor().getAlbumDao().getAlbumsForArtist(artist.getName(), allFolders);
    if (allAlbums.size() > 1) {
      Album viewAll = new Album();
      viewAll.setName("- All Albums -");
      viewAll.setId(-1);
      viewAll.setComment(AlbumUpnpProcessor.ALL_BY_ARTIST + "_" + artist.getId());
      allAlbums.add(0, viewAll);
    }
    return allAlbums;
  }

  public void addChild(DIDLContent didl, Album album) throws Exception {
    didl.addContainer(getAlbumProcessor().createContainer(album));
  }

  public ArtistDao getArtistDao() {
    return artistDao;
  }
  public void setArtistDao(ArtistDao artistDao) {
    this.artistDao = artistDao;
  }

  public AlbumUpnpProcessor getAlbumProcessor() {
    return getDispatcher().getAlbumProcessor();
  }
}
