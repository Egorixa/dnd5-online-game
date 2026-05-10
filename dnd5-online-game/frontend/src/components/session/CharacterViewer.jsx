import React, { useEffect, useRef, useState } from 'react';
import { Plus, Trash2, Dice5 } from 'lucide-react';
import CharacterBasicInfo from '../character/CharacterBasicInfo';
import AbilityScores from '../character/AbilityScores';
import SavingThrows from '../character/SavingThrows';
import Skills from '../character/Skills';
import CombatStats from '../character/CombatStats';
import Equipment from '../character/Equipment';
import PersonalityTraits from '../character/PersonalityTraits';
import Appearance from '../character/Appearance';
import Spells from '../character/Spells';

const rollD20 = () => 1 + Math.floor(Math.random() * 20);

const parseDamage = (formula, data) => {
  if (!formula || typeof formula !== 'string') return null;
  let cleaned = formula.replace(/\s+/g, '').toLowerCase();
  if (!cleaned) return null;

  if (data) {
    const calcMod = (score) => Math.floor(((score || 10) - 10) / 2);
    const stats = {
      str: calcMod(data.strength),
      dex: calcMod(data.dexterity),
      con: calcMod(data.constitution),
      int: calcMod(data.intelligence),
      wis: calcMod(data.wisdom),
      cha: calcMod(data.charisma),
    };
    for (const [key, val] of Object.entries(stats)) {
      cleaned = cleaned.replace(new RegExp(key, 'g'), val >= 0 ? `+${val}` : `${val}`);
    }
  }

  const tokenRe = /([+-]?)(\d*)d(\d+)|([+-]?\d+)/g;
  const groups = [];
  let mod = 0;
  let m;
  let matchedAny = false;
  while ((m = tokenRe.exec(cleaned)) !== null) {
    matchedAny = true;
    if (m[3] !== undefined) {
      const sign = m[1] === '-' ? -1 : 1;
      const n = Math.min(parseInt(m[2] || '1', 10) || 1, 20);
      const sides = parseInt(m[3], 10) || 6;
      if (sides < 2) continue;
      groups.push({ sign, n, sides });
    } else if (m[4] !== undefined) {
      mod += parseInt(m[4], 10) || 0;
    }
  }
  if (!matchedAny) return null;

  const rolls = [];
  let sum = 0;
  for (const g of groups) {
    for (let i = 0; i < g.n; i++) {
      const r = 1 + Math.floor(Math.random() * g.sides);
      rolls.push(r);
      sum += g.sign * r;
    }
  }
  return { rolls, mod, total: Math.max(0, sum + mod), formula };
};

const toData = (player) => ({
  ...player,
  name: player.characterName ?? player.name ?? '',
  playerName: player.username ?? player.playerName ?? '',
  class: player.class ?? player.characterClass ?? '',
  attacks: (player.attacks || []).map((a) => ({
    attackId: a.attackId,
    name: a.name || '',
    attackBonus: a.attackBonus ?? a.bonus ?? 0,
    damage: a.damage || '',
  })),
  spells: player.spells || [],
});

const fromData = (data) => {
  const copy = { ...data };
  delete copy.attacks;
  delete copy.spells;
  return {
    ...copy,
    characterName: data.name,
    username: data.playerName,
    characterClass: data.class,
  };
};

const DEBOUNCE_MS = 400;

const ATTACK_DEBOUNCE_MS = 500;
const NOTES_STORAGE_KEY = 'dnd_master_notes';

const readNotes = (charId) => {
  if (!charId) return '';
  try {
    const map = JSON.parse(localStorage.getItem(NOTES_STORAGE_KEY) || '{}');
    return map[charId] || '';
  } catch { return ''; }
};

const writeNotes = (charId, value) => {
  if (!charId) return;
  try {
    const map = JSON.parse(localStorage.getItem(NOTES_STORAGE_KEY) || '{}');
    if (value) map[charId] = value;
    else delete map[charId];
    localStorage.setItem(NOTES_STORAGE_KEY, JSON.stringify(map));
  } catch { /* ignore */ }
};

const CharacterViewer = ({
  player, onUpdate, onAttackRoll,
  onAddAttack, onUpdateAttack, onRemoveAttack,
  onAddSpell, onUpdateSpell, onRemoveSpell,
}) => {
  const [data, setData] = useState(() => toData(player));
  const [masterNotes, setMasterNotes] = useState(() => readNotes(player?.characterId));

  useEffect(() => {
    setMasterNotes(readNotes(player?.characterId));
  }, [player?.characterId]);

  const handleMasterNotesChange = (val) => {
    setMasterNotes(val);
    writeNotes(player?.characterId, val);
  };

  const dirtyRef = useRef(false);
  const debounceRef = useRef(null);
  const editSeqRef = useRef(0);
  const lastSyncedIdRef = useRef(player?.id);

  useEffect(() => {
    if (player?.id !== lastSyncedIdRef.current) {
      lastSyncedIdRef.current = player?.id;
      dirtyRef.current = false;
      editSeqRef.current = 0;
      if (debounceRef.current) clearTimeout(debounceRef.current);
      setData(toData(player));
      return;
    }
    if (dirtyRef.current) return;
    setData(toData(player));
  }, [player]);

  useEffect(() => () => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
  }, []);

  const handleChange = (newData) => {
    dirtyRef.current = true;
    const seq = ++editSeqRef.current;
    setData(newData);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(async () => {
      try {
        await onUpdate(player.id, fromData(newData));
      } finally {
        if (seq === editSeqRef.current) {
          dirtyRef.current = false;
        }
      }
    }, DEBOUNCE_MS);
  };

  const handleLocalChange = (newData) => {
    dirtyRef.current = true;
    const seq = ++editSeqRef.current;
    setData(newData);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      if (seq === editSeqRef.current) {
        dirtyRef.current = false;
      }
    }, 600);
  };

  const attacks = data.attacks;
  const attackTimersRef = useRef(new Map());

  const addAttack = async () => {
    if (attacks.length >= 20) return;
    const created = onAddAttack ? await onAddAttack(player.id, { name: '', attackBonus: 0, damage: '' }) : null;
    if (created?.attackId) {
      setData((prev) => {
        const existing = prev.attacks || [];
        if (existing.some((a) => a.attackId === created.attackId)) return prev;
        return { ...prev, attacks: [...existing, {
          attackId: created.attackId,
          name: created.name || '',
          attackBonus: created.attackBonus ?? 0,
          damage: created.damage || '',
        }] };
      });
    }
  };

  const removeAttack = async (i) => {
    const target = attacks[i];
    setData((prev) => ({ ...prev, attacks: (prev.attacks || []).filter((_, idx) => idx !== i) }));
    if (target?.attackId && onRemoveAttack) {
      const t = attackTimersRef.current.get(target.attackId);
      if (t) clearTimeout(t);
      attackTimersRef.current.delete(target.attackId);
      await onRemoveAttack(player.id, target.attackId);
    }
  };

  const updateAttack = (i, patch) => {
    const next = attacks.map((a, idx) => (idx === i ? { ...a, ...patch } : a));
    handleLocalChange({ ...data, attacks: next });
    const target = next[i];
    if (!target?.attackId || !onUpdateAttack) return;
    const existing = attackTimersRef.current.get(target.attackId);
    if (existing) clearTimeout(existing);
    const timer = setTimeout(() => {
      attackTimersRef.current.delete(target.attackId);
      onUpdateAttack(player.id, target.attackId, {
        name: target.name || '',
        attackBonus: target.attackBonus ?? 0,
        damage: target.damage || '',
      });
    }, ATTACK_DEBOUNCE_MS);
    attackTimersRef.current.set(target.attackId, timer);
  };

  useEffect(() => () => {
    attackTimersRef.current.forEach((t) => clearTimeout(t));
    attackTimersRef.current.clear();
  }, []);

  const rollAttack = (atk) => {
    const d20 = rollD20();
    const bonus = atk.attackBonus ?? 0;
    const attackTotal = d20 + bonus;
    const damage = parseDamage(atk.damage, data);
    onAttackRoll?.({ player, attack: atk, d20, bonus, attackTotal, damage });
  };

  return (
    <div className="char-viewer-full">
      <CharacterBasicInfo data={data} onChange={handleChange} />
      <AbilityScores data={data} onChange={handleChange} />
      <SavingThrows data={data} onChange={handleChange} />
      <Skills data={data} onChange={handleChange} />
      <CombatStats data={data} onChange={handleChange} />

      <div className="sheet-section">
        <h3 className="section-title">
          Атаки
          <button
            type="button"
            className="char-viewer-inline-btn"
            onClick={addAttack}
            title="Добавить атаку"
            disabled={attacks.length >= 20}
          >
            <Plus size={14} />
          </button>
        </h3>
        {attacks.length === 0 ? (
          <div className="char-viewer-empty">Нет атак. Добавьте первую.</div>
        ) : (
          <div className="char-viewer-table-wrap">
            <table className="char-viewer-attacks-table">
              <thead>
                <tr>
                  <th>Название</th>
                  <th>Бонус</th>
                  <th>Урон</th>
                  <th />
                  <th />
                </tr>
              </thead>
              <tbody>
                {attacks.map((a, i) => (
                  <tr key={i}>
                    <td>
                      <input
                        className="form-input"
                        value={a.name || ''}
                        maxLength={50}
                        onChange={(e) => updateAttack(i, { name: e.target.value })}
                      />
                    </td>
                    <td>
                      <input
                        className="form-input"
                        type="number"
                        min={-20}
                        max={20}
                        value={a.attackBonus ?? 0}
                        onChange={(e) =>
                          updateAttack(i, {
                            attackBonus: Math.max(-20, Math.min(20, parseInt(e.target.value) || 0)),
                          })
                        }
                      />
                    </td>
                    <td>
                      <input
                        className="form-input"
                        value={a.damage || ''}
                        maxLength={50}
                        placeholder="1d8+3"
                        onChange={(e) => updateAttack(i, { damage: e.target.value })}
                      />
                    </td>
                    <td>
                      <button
                        type="button"
                        className="char-viewer-icon-btn char-viewer-roll-btn"
                        onClick={() => rollAttack(a)}
                        title="Бросить атаку"
                      >
                        <Dice5 size={14} />
                      </button>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="char-viewer-icon-btn"
                        onClick={() => removeAttack(i)}
                        title="Удалить"
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="section-hint" style={{ marginTop: '8px' }}>
              Подсказка: в поле "Урон" можно использовать статы, например <code>1d8+str</code> или <code>2d6+dex</code>.
            </p>
          </div>
        )}
      </div>

      <Spells
        data={data}
        onChange={handleChange}
        onLocalChange={handleLocalChange}
        playerId={player.id}
        onAddSpell={onAddSpell}
        onUpdateSpell={onUpdateSpell}
        onRemoveSpell={onRemoveSpell}
      />
      <Equipment data={data} onChange={handleChange} />
      <PersonalityTraits data={data} onChange={handleChange} />
      <Appearance data={data} onChange={handleChange} />

      <div className="sheet-section">
        <h3 className="section-title">Заметки мастера</h3>
        <p className="section-hint">Видны только вам, не уходят на сервер.</p>
        <textarea
          className="form-textarea"
          rows={3}
          maxLength={2000}
          value={masterNotes}
          placeholder="Заметки мастера..."
          onChange={(e) => handleMasterNotesChange(e.target.value)}
        />
      </div>
    </div>
  );
};

export default CharacterViewer;
