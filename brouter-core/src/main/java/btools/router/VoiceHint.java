/**
 * Container for a voice hint
 * (both input- and result data for voice hint processing)
 *
 * @author ab
 */
package btools.router;

import java.util.ArrayList;
import java.util.List;

public class VoiceHint
{
  static final int C = 1; // continue (go straight)
  static final int TL = 2; // turn left
  static final int TSLL = 3; // turn slightly left
  static final int TSHL = 4; // turn sharply left
  static final int TR = 5; // turn right
  static final int TSLR = 6; // turn slightly right
  static final int TSHR = 7; // turn sharply right
  static final int KL = 8; // keep left
  static final int KR = 9; // keep right
  static final int TU = 10; // U-turn
  static final int TRU = 11; // Right U-turn
  static final int OFFR = 12; // Off route
  static final int RNDB = 13; // Roundabout
  static final int RNLB = 14; // Roundabout left

  int ilon;
  int ilat;
  int cmd;
  MessageData oldWay;
  MessageData goodWay;
  List<MessageData> badWays;
  double distanceToNext;
  int indexInTrack;

  float angle;
  boolean turnAngleConsumed;
  boolean needsRealTurn;

  int roundaboutExit;

  boolean isRoundabout()
  {
    return roundaboutExit != 0;
  }
  
  public void addBadWay( MessageData badWay )
  {
    if ( badWay == null )
    {
      return;
    }
    if ( badWays == null )
    {
      badWays = new ArrayList<MessageData>();
    }
    badWays.add( badWay );
  }

  public String getCommandString()
  {
    switch ( cmd )
    {
      case TU   : return "TU";
      case TSHL : return "TSHL";
      case TL   : return "TL";
      case TSLL : return "TSLL";
      case KL   : return "KL";
      case C    : return "C";
      case KR   : return "KR";
      case TSLR : return "TSLR";
      case TR   : return "TR";
      case TSHR : return "TSHR";
      case TRU  : return "TRU";
      case RNDB : return "RNDB" + roundaboutExit;
      case RNLB : return "RNLB" + (-roundaboutExit);
      default : throw new IllegalArgumentException( "unknown command: " + cmd );
    }
  }
  
  public String getSymbolString()
  {
    switch ( cmd )
    {
      case TU   : return "TU";
      case TSHL : return "TSHL";
      case TL   : return "Left";
      case TSLL : return "TSLL";
      case KL   : return "TSLL"; // ?
      case C    : return "Straight";
      case KR   : return "TSLR"; // ?
      case TSLR : return "TSLR";
      case TR   : return "Right";
      case TSHR : return "TSHR";
      case TRU  : return "TU";
      case RNDB : return "RNDB" + roundaboutExit;
      case RNLB : return "RNLB" + (-roundaboutExit);
      default : throw new IllegalArgumentException( "unknown command: " + cmd );
    }
  }

  public String getMessageString()
  {
    switch ( cmd )
    {
      case TU   : return "u-turn";
      case TSHL : return "sharp left";
      case TL   : return "left";
      case TSLL : return "slight left";
      case KL   : return "keep left";
      case C    : return "straight";
      case KR   : return "keep right";
      case TSLR : return "slight right";
      case TR   : return "right";
      case TSHR : return "sharp right";
      case TRU  : return "u-turn";
      case RNDB : return "Take exit " + roundaboutExit;
      case RNLB : return "Take exit " + (-roundaboutExit);
      default : throw new IllegalArgumentException( "unknown command: " + cmd );
    }
  }

  public int getLocusAction()
  {
    switch ( cmd )
    {
      case TU   : return 12;
      case TSHL : return 5;
      case TL   : return 4;
      case TSLL : return 3;
      case KL   : return 9; // ?
      case C    : return 1;
      case KR   : return 10; // ?
      case TSLR : return 6;
      case TR   : return 7;
      case TSHR : return 8;
      case TRU  : return 12;
      case RNDB : return 26 + roundaboutExit;
      case RNLB : return 26 - roundaboutExit;
      default : throw new IllegalArgumentException( "unknown command: " + cmd );
    }
  }

  public void calcCommand()
  {
    if (roundaboutExit > 0)
    {
      cmd = RNDB;
    }
    else if (roundaboutExit < 0)
    {
      cmd = RNLB;
    }
    else if ( angle < -159. )
    {
      cmd = TU;
    }
    else if ( angle < -113. )
    {
      cmd = TSHL;
    }
    else if ( angle < -67. )
    {
      cmd = TL;
    }
    else if ( angle < -21. )
    {
      if ( cmd != KR ) // don't overwrite KR with TSLL
      {
        cmd = TSLL;
      }
    }
    else if ( angle < 21. )
    {
      if ( cmd != KR && cmd != KL ) // don't overwrite KL/KR hints!
      {
        cmd = C;
      }
    }
    else if ( angle < 67. )
    {
      if ( cmd != KL ) // don't overwrite KL with TSLR
      {
        cmd = TSLR;
      }
    }
    else if ( angle < 113. )
    {
      cmd = TR;
    }
    else if ( angle < 159. )
    {
      cmd = TSHR;
    }
    else
    {
      cmd = TRU;
    }
  }

  public String formatGeometry()
  {
    float oldPrio = oldWay == null ? 0.f : oldWay.priorityclassifier;
    StringBuilder sb = new StringBuilder(30);
    sb.append( ' ' ).append( (int)oldPrio );
    appendTurnGeometry(sb,goodWay);
    if ( badWays != null )
    {
      for ( MessageData badWay : badWays )
      {
        sb.append( " " );
        appendTurnGeometry( sb, badWay );
      }
    }
    return sb.toString();
  }

  private void appendTurnGeometry( StringBuilder sb, MessageData msg )
  {
    sb.append( "(" ).append( (int)(msg.turnangle+0.5) ).append( ")" ).append( (int)(msg.priorityclassifier) );
  }

}
