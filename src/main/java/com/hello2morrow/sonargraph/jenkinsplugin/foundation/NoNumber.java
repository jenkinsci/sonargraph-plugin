package com.hello2morrow.sonargraph.jenkinsplugin.foundation;

public class NoNumber extends Number
{
    public static final Number NULL_NUMBER = new NoNumber("");
    private static final long serialVersionUID = -694256230936634135L;
    private final String m_String;

    public NoNumber(String string)
    {
        assert string != null : "'string' must not be null";
        m_String = string;
    }

    @Override
    public double doubleValue()
    {
        return 0;
    }

    @Override
    public float floatValue()
    {
        return 0;
    }

    @Override
    public int intValue()
    {
        return 0;
    }

    @Override
    public long longValue()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        return m_String;
    }
}
