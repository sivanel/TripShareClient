package com.TripShare.Client.Common;

import java.io.Serializable;
import java.util.*;

public class Route implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long m_ID;
    private long m_userID;
    private String m_routeName;
    private String m_createdDate;
    private List<Coordinate> m_routeCoordinates= new ArrayList<>();

    public Route(long i_userID)
    {
        m_userID = i_userID;
    }

    public long getUserID()
    {
        return m_userID;
    }

    public long getRouteID()
    {
        return m_ID;
    }

    public String getRouteName()
    {
        return m_routeName;
    }

    public List<Coordinate> getRouteCoordinates()
    {
        return m_routeCoordinates;
    }

    public void addCoordinateToRoute(Coordinate i_newCoordinate)
    {
        m_routeCoordinates.add(i_newCoordinate);
    }

    public void setRouteName(String i_routeName)
    {
        m_routeName = new String(i_routeName);
    }

    public String getCreatedDate() { return m_createdDate; }

    public void setCreatedDate(String i_createdDate) { m_createdDate = i_createdDate; }

    public void setRouteID(long i_ID) { m_ID = i_ID; }
}

