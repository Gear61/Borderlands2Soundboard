package com.claptrapsoundboard;

public class Ranking
{
	private String m_rank;
	private int m_total_plays;

	// Default constructor
	public Ranking()
	{
	}

	// Constructor
	public Ranking(String rank, int totalPlays)
	{
		m_rank = rank;
		m_total_plays = totalPlays;
	}

	// Getters and setters
	public String getRank()
	{
		return m_rank;
	}

	public void setRank(String rank)
	{
		m_rank = rank;
	}

	public int getTotalPlays()
	{
		return m_total_plays;
	}

	public void setCharacter(int totalPlays)
	{
		m_total_plays = totalPlays;
	}
}
