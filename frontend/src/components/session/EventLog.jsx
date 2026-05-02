import React, { useRef, useEffect } from 'react';
import { Dice1, UserPlus, UserMinus, MessageCircle } from 'lucide-react';

const iconMap = {
  dice: Dice1,
  join: UserPlus,
  leave: UserMinus,
  system: MessageCircle,
};

const EventLog = ({ events }) => {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [events.length]);

  const formatTime = (ts) => {
    const d = new Date(ts);
    return d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  return (
    <div className="event-log">
      <h3 className="panel-title">Журнал событий</h3>
      <div className="event-log-list">
        {events.length === 0 && (
          <p className="event-log-empty">События будут отображаться здесь</p>
        )}
        {events.map((ev, i) => {
          const Icon = iconMap[ev.type] || MessageCircle;
          return (
            <div key={i} className={`event-log-item event-type-${ev.type}`}>
              <Icon size={14} />
              <span className="event-log-text">{ev.text}</span>
              <span className="event-log-time">{formatTime(ev.timestamp)}</span>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>
    </div>
  );
};

export default EventLog;
