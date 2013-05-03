package com.hello2morrow.sonargraph.jenkinsplugin.model;

public class InvalidDataPoint implements IDataPoint
{
    private int m_x;

    public InvalidDataPoint(int x)
    {
        m_x = x;
    }

    public int getX()
    {
        return m_x;
    }

    public double getY()
    {
        return Double.NaN;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + m_x;
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
        InvalidDataPoint other = (InvalidDataPoint) obj;
        if (m_x != other.m_x)
        {
            return false;
        }
        return true;
    }

}
