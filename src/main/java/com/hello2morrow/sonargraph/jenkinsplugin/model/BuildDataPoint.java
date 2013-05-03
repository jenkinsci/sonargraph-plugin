package com.hello2morrow.sonargraph.jenkinsplugin.model;

import java.util.GregorianCalendar;

import com.hello2morrow.sonargraph.jenkinsplugin.foundation.StringUtility;

public class BuildDataPoint implements IDataPoint
{
    private double m_value;
    private int m_buildNumber;
    private long m_timestamp;

    public BuildDataPoint(int buildNumber, double value, long timeInMillis)
    {
        m_buildNumber = buildNumber;
        m_value = value;
        m_timestamp = timeInMillis;
    }

    /* (non-Javadoc)
     * @see com.hello2morrow.sonargraph.jenkinsplugin.model.DataPoint#getX()
     */
    public int getX()
    {
        return m_buildNumber;
    }

    /* (non-Javadoc)
     * @see com.hello2morrow.sonargraph.jenkinsplugin.model.DataPoint#getY()
     */
    public double getY()
    {
        return m_value;
    }

    public long getTimestamp()
    {
        return m_timestamp;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + m_buildNumber;
        result = (prime * result) + (int) (m_timestamp ^ (m_timestamp >>> 32));
        long temp;
        temp = Double.doubleToLongBits(m_value);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        BuildDataPoint other = (BuildDataPoint) obj;
        if (m_buildNumber != other.m_buildNumber)
        {
            return false;
        }
        if (m_timestamp != other.m_timestamp)
        {
            return false;
        }
        if (Double.doubleToLongBits(m_value) != Double.doubleToLongBits(other.m_value))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(getTimestamp());
        return "Build number: " + m_buildNumber + ", value: " + m_value + ", date/time: " + StringUtility.formatDateTime(calendar);
    }
}
