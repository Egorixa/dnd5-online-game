import React, { useMemo, useState } from 'react';
import { Plus, Trash2, ChevronRight, Play, Square, HelpCircle, Swords } from 'lucide-react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';

const rollD20 = () => 1 + Math.floor(Math.random() * 20);

const InitiativeTracker = ({ players = [], selectedPlayerId, onSelectPlayer, onUpdatePlayer, onLogEvent }) => {
  const [npcs, setNpcs] = useState([]);
  const [rolls, setRolls] = useState({});
  const [activeId, setActiveId] = useState(null);
  const [combatActive, setCombatActive] = useState(false);
  const [showAddNpc, setShowAddNpc] = useState(false);
  const [showHelp, setShowHelp] = useState(false);
  const [npcName, setNpcName] = useState('');
  const [npcInit, setNpcInit] = useState(10);
  const [npcHp, setNpcHp] = useState(10);
  const [damageTarget, setDamageTarget] = useState(null);
  const [damageValue, setDamageValue] = useState(5);
  const [damageError, setDamageError] = useState('');

  const combatants = useMemo(() => {
    const playerEntries = players.map((p) => ({
      id: p.id,
      name: p.characterName || 'Игрок',
      initiative: rolls[p.id] ?? null,
      hpCurrent: p.hpCurrent ?? 0,
      hpMax: p.hpMax ?? 0,
      isPlayer: true,
    }));
    const all = [...playerEntries, ...npcs];
    return all.sort((a, b) => {
      const ai = a.initiative ?? -Infinity;
      const bi = b.initiative ?? -Infinity;
      return bi - ai;
    });
  }, [players, npcs, rolls]);

  const startCombat = () => {
    const newRolls = {};
    players.forEach((p) => {
      const d20 = rollD20();
      const bonus = p.initiativeBonus ?? 0;
      newRolls[p.id] = d20 + bonus;
      onLogEvent?.({
        type: 'dice',
        text: `${p.characterName || 'Игрок'}: d20(${d20}) + ${bonus} = ${d20 + bonus} (инициатива)`,
      });
    });
    setRolls(newRolls);
    const first = [...players, ...npcs]
      .map((c) => ({ id: c.id, init: newRolls[c.id] ?? c.initiative ?? 0 }))
      .sort((a, b) => b.init - a.init)[0];
    setActiveId(first?.id || null);
    setCombatActive(true);
    onLogEvent?.({ type: 'system', text: 'Бой начат. Инициатива разыграна.' });
  };

  const endCombat = () => {
    setNpcs([]);
    setRolls({});
    setActiveId(null);
    setCombatActive(false);
    onLogEvent?.({ type: 'system', text: 'Бой завершён.' });
  };

  const addNpc = () => {
    if (!npcName.trim()) return;
    const id = 'npc-' + Date.now().toString();
    setNpcs((prev) => [
      ...prev,
      {
        id,
        name: npcName.trim(),
        initiative: npcInit,
        hpCurrent: npcHp,
        hpMax: npcHp,
        isPlayer: false,
      },
    ]);
    setNpcName('');
    setNpcInit(10);
    setNpcHp(10);
    setShowAddNpc(false);
  };

  const removeNpc = (id) => {
    setNpcs((prev) => prev.filter((n) => n.id !== id));
    if (activeId === id) nextTurn();
  };

  const nextTurn = () => {
    if (combatants.length === 0) return;
    if (!activeId) {
      setActiveId(combatants[0].id);
      return;
    }
    const idx = combatants.findIndex((c) => c.id === activeId);
    const next = combatants[(idx + 1) % combatants.length];
    setActiveId(next.id);
    onLogEvent?.({ type: 'system', text: `Ход: ${next.name}` });
  };

  const applyDamageToPlayer = (pl, amount) => {
    let remaining = amount;
    let tempHp = pl.hpTemp || 0;
    if (tempHp > 0) {
      const absorbed = Math.min(tempHp, remaining);
      tempHp -= absorbed;
      remaining -= absorbed;
    }
    const newHp = Math.max(0, (pl.hpCurrent || 0) - remaining);
    onUpdatePlayer?.(pl.id, { ...pl, hpCurrent: newHp, hpTemp: tempHp });
    onLogEvent?.({
      type: 'system',
      text: `${pl.characterName || 'Игрок'}: −${amount} HP → ${newHp}/${pl.hpMax ?? 0}`,
    });
  };

  const openDamageModal = (c) => {
    setDamageTarget(c);
    setDamageValue(5);
    setDamageError('');
  };

  const closeDamageModal = () => {
    setDamageTarget(null);
    setDamageError('');
  };

  const applyDamageFromModal = () => {
    const n = parseInt(damageValue, 10);
    if (!Number.isFinite(n) || n <= 0) {
      setDamageError('Введите положительное число');
      return;
    }
    const c = damageTarget;
    if (!c) return;
    if (c.isPlayer) {
      const pl = players.find((p) => p.id === c.id);
      if (pl) applyDamageToPlayer(pl, n);
    } else {
      setNpcs((prev) =>
        prev.map((x) => (x.id === c.id ? { ...x, hpCurrent: Math.max(0, (x.hpCurrent || 0) - n) } : x))
      );
      onLogEvent?.({ type: 'system', text: `${c.name}: −${n} HP` });
    }
    closeDamageModal();
  };

  const selectCombatant = (c) => {
    if (c.isPlayer) onSelectPlayer?.(c.id);
  };

  return (
    <div className="initiative-tracker">
      <div className="initiative-header">
        <h3 className="panel-title">Боевой трекер</h3>
        <div className="initiative-actions">
          <button className="init-action-btn" onClick={() => setShowHelp((v) => !v)} title="Как вести бой">
            <HelpCircle size={18} />
          </button>
          <button
            className="init-action-btn"
            onClick={() => setShowAddNpc((v) => !v)}
            title="Добавить НИП"
          >
            <Plus size={18} />
          </button>
          {combatActive ? (
            <button className="init-action-btn init-stop" onClick={endCombat} title="Завершить бой">
              <Square size={18} />
            </button>
          ) : (
            <button className="init-action-btn init-start" onClick={startCombat} title="Начать бой">
              <Play size={18} />
            </button>
          )}
        </div>
      </div>

      {showHelp && (
        <div className="init-help">
          <div className="init-help-title">Как вести бой</div>
          <ol className="init-help-list">
            <li>Кнопка ▶ — начать бой: система сама бросает d20+инициативу за каждого игрока.</li>
            <li>НИП добавляется через +: имя, готовая инициатива, HP.</li>
            <li>Клик по игроку в трекере — открывает его лист персонажа в центре.</li>
            <li>Атака: в листе персонажа жмите иконку кубика рядом с атакой — авто-бросок d20+бонус и урона.</li>
            <li>Урон: иконка ⚔ у записи трекера — запросит число и применит (через врем. HP).</li>
            <li>«Следующий ход» — передать инициативу дальше по сортировке.</li>
            <li>■ — завершить бой (сбросит НИП и инициативу).</li>
          </ol>
        </div>
      )}

      {showAddNpc && (
        <div className="init-add-npc">
          <input
            className="form-input init-input"
            placeholder="Имя НИП"
            value={npcName}
            onChange={(e) => setNpcName(e.target.value)}
            maxLength={30}
          />
          <input
            className="form-input init-input-small"
            type="number"
            placeholder="Иниц."
            value={npcInit}
            onChange={(e) => setNpcInit(parseInt(e.target.value) || 0)}
          />
          <input
            className="form-input init-input-small"
            type="number"
            placeholder="HP"
            value={npcHp}
            onChange={(e) => setNpcHp(parseInt(e.target.value) || 1)}
            min={1}
          />
          <button className="init-action-btn" onClick={addNpc}>OK</button>
        </div>
      )}

      <div className="init-list">
        {combatants.length === 0 && (
          <div className="init-empty">Нет участников. Нажмите ▶ для начала.</div>
        )}
        {combatants.map((c, i) => {
          const isActive = c.id === activeId;
          const isSelected = c.isPlayer && c.id === selectedPlayerId;
          return (
            <div
              key={c.id}
              className={`init-row ${isActive ? 'init-row-active' : ''} ${isSelected ? 'init-row-selected' : ''} ${c.isPlayer ? 'init-row-player' : ''}`}
              onClick={() => selectCombatant(c)}
            >
              <span className="init-order">{i + 1}</span>
              <span className="init-name">
                {c.name}
                {!c.isPlayer && <span className="init-npc-badge">НИП</span>}
              </span>
              <span className="init-value">{c.initiative ?? '—'}</span>
              <span className="init-hp">{c.hpCurrent}/{c.hpMax}</span>
              <button
                className="init-action-btn init-dmg-btn"
                onClick={(e) => { e.stopPropagation(); openDamageModal(c); }}
                title="Нанести урон"
              >
                <Swords size={16} />
              </button>
              {!c.isPlayer && (
                <button
                  className="init-remove-btn"
                  onClick={(e) => { e.stopPropagation(); removeNpc(c.id); }}
                  title="Удалить"
                >
                  <Trash2 size={16} />
                </button>
              )}
            </div>
          );
        })}
      </div>

      {combatActive && combatants.length > 0 && (
        <button className="init-next-btn" onClick={nextTurn}>
          <ChevronRight size={20} /> Следующий ход
        </button>
      )}

      <Modal
        isOpen={!!damageTarget}
        onClose={closeDamageModal}
        title={damageTarget ? `Урон по «${damageTarget.name}»` : ''}
      >
        <div className="input-group">
          <label className="input-label">Количество урона</label>
          <input
            className={`form-input ${damageError ? 'input-error' : ''}`}
            type="number"
            min={1}
            max={9999}
            value={damageValue}
            onChange={(e) => {
              setDamageValue(e.target.value);
              if (damageError) setDamageError('');
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') applyDamageFromModal();
            }}
            autoFocus
          />
          {damageError && <span className="error-text">{damageError}</span>}
        </div>
        <div className="modal-actions">
          <Button variant="secondary" onClick={closeDamageModal}>Отмена</Button>
          <Button variant="danger" onClick={applyDamageFromModal}>Нанести урон</Button>
        </div>
      </Modal>
    </div>
  );
};

export default InitiativeTracker;
