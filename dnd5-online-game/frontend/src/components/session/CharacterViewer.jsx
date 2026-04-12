// Полный лист персонажа в сессии мастера (ТЗ 4.1.2). Реиспользует секции из components/character/*.
import React, { useMemo } from 'react';
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

const parseDamage = (formula) => {
  if (!formula) return null;
  const m = /(\d+)\s*d\s*(\d+)\s*(?:([+-])\s*(\d+))?/i.exec(formula);
  if (!m) return null;
  const n = Math.min(parseInt(m[1], 10) || 1, 20);
  const sides = parseInt(m[2], 10) || 6;
  const sign = m[3] === '-' ? -1 : 1;
  const mod = m[4] ? sign * parseInt(m[4], 10) : 0;
  const rolls = [];
  let sum = 0;
  for (let i = 0; i < n; i++) {
    const r = 1 + Math.floor(Math.random() * sides);
    rolls.push(r);
    sum += r;
  }
  return { rolls, mod, total: Math.max(0, sum + mod), formula };
};

const toData = (player) => ({
  ...player,
  name: player.characterName ?? player.name ?? '',
  playerName: player.username ?? player.playerName ?? '',
  attacks: (player.attacks || []).map((a) => ({
    name: a.name || '',
    attackBonus: a.attackBonus ?? a.bonus ?? 0,
    damage: a.damage || '',
  })),
});

const fromData = (data) => ({
  ...data,
  characterName: data.name,
  username: data.playerName,
  attacks: (data.attacks || []).map((a) => ({
    name: a.name || '',
    bonus: a.attackBonus ?? 0,
    attackBonus: a.attackBonus ?? 0,
    damage: a.damage || '',
  })),
});

const CharacterViewer = ({ player, onUpdate, onAttackRoll }) => {
  const data = useMemo(() => toData(player), [player]);

  const handleChange = (newData) => {
    onUpdate(player.id, fromData(newData));
  };

  const attacks = data.attacks;
  const addAttack = () => {
    if (attacks.length >= 20) return;
    handleChange({ ...data, attacks: [...attacks, { name: '', attackBonus: 0, damage: '' }] });
  };
  const removeAttack = (i) => {
    handleChange({ ...data, attacks: attacks.filter((_, idx) => idx !== i) });
  };
  const updateAttack = (i, patch) => {
    const next = attacks.map((a, idx) => (idx === i ? { ...a, ...patch } : a));
    handleChange({ ...data, attacks: next });
  };

  const rollAttack = (atk) => {
    const d20 = rollD20();
    const bonus = atk.attackBonus ?? 0;
    const attackTotal = d20 + bonus;
    const damage = parseDamage(atk.damage);
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
                        className="char-viewer-icon-btn char-viewer-roll-btn"
                        onClick={() => rollAttack(a)}
                        title="Бросить атаку"
                      >
                        <Dice5 size={14} />
                      </button>
                    </td>
                    <td>
                      <button
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
          </div>
        )}
      </div>

      <Spells data={data} onChange={handleChange} />
      <Equipment data={data} onChange={handleChange} />
      <PersonalityTraits data={data} onChange={handleChange} />
      <Appearance data={data} onChange={handleChange} />

      <div className="sheet-section">
        <h3 className="section-title">Заметки мастера</h3>
        <textarea
          className="form-textarea"
          rows={3}
          maxLength={2000}
          value={data.notes || ''}
          placeholder="Заметки мастера..."
          onChange={(e) => handleChange({ ...data, notes: e.target.value })}
        />
      </div>
    </div>
  );
};

export default CharacterViewer;
