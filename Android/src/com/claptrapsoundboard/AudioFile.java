package com.claptrapsoundboard;

public class AudioFile
{
	private String m_name;
	private String m_character;

	// Default constructor
	public AudioFile()
	{
	}

	// Constructor
	public AudioFile(String name, String character)
	{
		m_name = name;
		m_character = character;
	}

	// Getters and setters
	public String getName()
	{
		return m_name;
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public String getCharacter()
	{
		return m_character;
	}

	public void setCharacter(String character)
	{
		m_character = character;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString()
	{
		return m_name;
	}
}
