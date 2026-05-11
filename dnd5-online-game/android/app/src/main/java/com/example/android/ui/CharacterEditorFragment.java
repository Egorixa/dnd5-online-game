package com.example.android.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.android.R;
import com.example.android.data.AppDatabase;
import com.example.android.data.DndData;
import com.example.android.data.SessionManager;
import com.example.android.data.dao.CharacterDao;
import com.example.android.data.model.Character;
import com.example.android.net.ApiClient;
import com.example.android.net.ApiErrors;
import com.example.android.net.dto.CharacterDtos;
import com.example.android.net.mapper.CharacterMapper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CharacterEditorFragment extends Fragment {

    public static final String ARG_CHARACTER_ID = "character_id";

    public static final String ARG_ROOM_ID = "room_id";

    public static final String ARG_SERVER_CHARACTER_ID = "server_character_id";
    private static final int MAX_ATTACKS = 20;
    private static final int MAX_SPELLS = 30;

    private static final String[] PROF_LABELS_3 = {"Нет", "Влд", "Комп"};
    private static final String[] PROF_LABELS_2 = {"Нет", "Влд"};

    private Button[] saveButtons = new Button[6];
    private TextView[] saveValues = new TextView[6];

    private static final String[] SKILL_NAMES = {
            "Акробатика", "Уход за животными", "Магия", "Атлетика",
            "Обман", "История", "Проницательность", "Запугивание",
            "Анализ", "Медицина", "Природа", "Внимательность",
            "Выступление", "Убеждение", "Религия",
            "Ловкость рук", "Скрытность", "Выживание"
    };

    private static final String[] SKILL_ABILITIES = {
            "dex", "wis", "int", "str",
            "cha", "int", "wis", "cha",
            "int", "wis", "int", "wis",
            "cha", "cha", "int",
            "dex", "dex", "wis"
    };

    private final Button[] skillButtons = new Button[SKILL_NAMES.length];
    private final TextView[] skillValues = new TextView[SKILL_NAMES.length];

    private EditText[] abilityValueEt = new EditText[6];
    private TextView[] abilityModTv = new TextView[6];
    private static final String[] ABILITY_LABELS = {"СИЛ", "ЛОВ", "ТЕЛ", "ИНТ", "МДР", "ХАР"};

    private TextInputEditText etCharName, etLevel, etXp;
    private AutoCompleteTextView spinnerRace, spinnerClass, spinnerBackground, spinnerAlignment, spinnerHitDie;
    private TextInputEditText etAc, etInitBonus, etSpeed;
    private TextInputEditText etHpMax, etHpCur, etHpTemp, etHitDieCurrent;
    private CheckBox cbInsp;
    private CheckBox cbDeathS1, cbDeathS2, cbDeathS3, cbDeathF1, cbDeathF2, cbDeathF3;
    private TextInputEditText etEquipment, etOtherProf;
    private TextInputEditText etCp, etSp, etEp, etGp, etPp;
    private TextInputEditText etPersonality, etIdeals, etBonds, etFlaws, etFeatures, etBackstory;
    private TextInputEditText etAge, etHeight, etWeight, etEyes, etSkin, etHair;
    private TextInputEditText etAllies, etTreasure, etMarks, etNotes;

    private TextView tvProfBonus, tvInitiativeCalc, tvHitDieTotal;
    private TextView tvSpellClassInfo, tvSpellDcInfo;
    private LinearLayout spellsSection;

    private LinearLayout attacksContainer;
    private final List<View> attackRows = new ArrayList<>();
    private LinearLayout spellsContainer;
    private final List<View> spellRows = new ArrayList<>();
    private static final Gson GSON = new Gson();

    @Nullable
    private Character editing;

    private boolean roomMode = false;
    private String roomModeRoomId = "";
    private String roomModeCharacterId = "";

    private static final long AUTO_SAVE_DELAY_MS = 1200L;
    private android.os.Handler autoSaveHandler;
    private final Runnable autoSaveTask = this::performAutoSave;
    private boolean suppressWatchers = false;
    private boolean autoSaveInProgress = false;
    private boolean autoSavePending = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupDropdowns();
        setupListeners();
        setupAttacks(view);
        setupSpells(view);

        Bundle args = getArguments();
        roomModeRoomId = args != null ? args.getString(ARG_ROOM_ID, "") : "";
        roomModeCharacterId = args != null ? args.getString(ARG_SERVER_CHARACTER_ID, "") : "";
        roomMode = !TextUtils.isEmpty(roomModeRoomId) && !TextUtils.isEmpty(roomModeCharacterId);

        if (roomMode) {

            loadFromRoomServer();
        } else {

            if (args != null) {
                int id = args.getInt(ARG_CHARACTER_ID, -1);
                if (id != -1) {
                    editing = AppDatabase.getInstance(requireContext())
                            .characterDao().findById(id);
                }
            }
            if (editing != null) {
                fillFromCharacter(editing);
            }
        }
        recalcAll();

        View btnSave = view.findViewById(R.id.btn_save_char);
        btnSave.setOnClickListener(v -> save(false));

        View btnSync = view.findViewById(R.id.btn_sync_char);
        if (btnSync != null) {
            btnSync.setVisibility(View.GONE);
        }


        if (roomMode) {
            lockRaceClassFields();
            setAllFieldsReadOnly((ViewGroup) view);
            btnSave.setVisibility(View.GONE);
        } else {
            if (editing != null) {
                lockIdentityFields();
                autoSaveHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                setupAutoSaveWatchers((ViewGroup) view);
            }
            btnSave.setVisibility(View.VISIBLE);
        }
    }

    private void setAllFieldsReadOnly(ViewGroup root) {
        if (root == null) return;
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof EditText) {
                child.setEnabled(false);
                child.setFocusable(false);
                child.setFocusableInTouchMode(false);
            } else if (child instanceof AutoCompleteTextView) {
                child.setEnabled(false);
                child.setFocusable(false);
                child.setClickable(false);
            } else if (child instanceof CheckBox) {
                child.setEnabled(false);
                child.setClickable(false);
            } else if (child instanceof Button || child instanceof ImageButton) {
                if (child.getId() != R.id.btn_sync_char) {
                    child.setEnabled(false);
                    child.setClickable(false);
                }
            } else if (child instanceof ViewGroup) {
                setAllFieldsReadOnly((ViewGroup) child);
            }
        }
    }

    private void lockIdentityFields() {
        if (etCharName != null && !TextUtils.isEmpty(textOf(etCharName))) {
            etCharName.setEnabled(false);
            etCharName.setFocusable(false);
        }
        if (spinnerRace != null && !TextUtils.isEmpty(textOf(spinnerRace))) {
            spinnerRace.setEnabled(false);
            spinnerRace.setFocusable(false);
            spinnerRace.setClickable(false);
        }
        if (spinnerClass != null && !TextUtils.isEmpty(textOf(spinnerClass))) {
            spinnerClass.setEnabled(false);
            spinnerClass.setFocusable(false);
            spinnerClass.setClickable(false);
        }
    }

    private void setupAutoSaveWatchers(ViewGroup root) {
        if (root == null) return;
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof EditText) {
                ((EditText) child).addTextChangedListener(autoSaveTextWatcher());
            } else if (child instanceof CheckBox) {
                ((CheckBox) child).setOnCheckedChangeListener((b, checked) -> {
                    if (!suppressWatchers) scheduleAutoSave();
                });
            } else if (child instanceof ViewGroup) {
                setupAutoSaveWatchers((ViewGroup) child);
            }
        }
    }

    private TextWatcher autoSaveTextWatcher() {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (!suppressWatchers) scheduleAutoSave();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void scheduleAutoSave() {
        if (autoSaveHandler == null || roomMode) return;
        autoSavePending = true;
        autoSaveHandler.removeCallbacks(autoSaveTask);
        autoSaveHandler.postDelayed(autoSaveTask, AUTO_SAVE_DELAY_MS);
    }

    private void performAutoSave() {
        if (!isAdded() || roomMode) return;
        if (autoSaveInProgress) return;
        String name = textOf(etCharName);
        String race = textOf(spinnerRace);
        String cls = textOf(spinnerClass);
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(race) || TextUtils.isEmpty(cls)) return;
        autoSaveInProgress = true;
        autoSavePending = false;
        try {
            save(true);
        } finally {
            autoSaveInProgress = false;
        }
    }

    @Override
    public void onDestroyView() {
        if (autoSaveHandler != null) {
            autoSaveHandler.removeCallbacks(autoSaveTask);
            if (autoSavePending && !roomMode) {
                performAutoSave();
            }
        }
        super.onDestroyView();
    }

    private void lockRaceClassFields() {

        if (spinnerRace != null) {
            spinnerRace.setEnabled(false);
            spinnerRace.setFocusable(false);
            spinnerRace.setClickable(false);
        }
        if (spinnerClass != null) {
            spinnerClass.setEnabled(false);
            spinnerClass.setFocusable(false);
            spinnerClass.setClickable(false);
        }
    }

    public void reloadFromServerIfRoomMode() {
        if (!isAdded() || !roomMode) return;
        loadFromRoomServer();
    }

    private void loadFromRoomServer() {
        int userId = new SessionManager(requireContext()).getUserId();
        ApiClient.get(requireContext()).characters()
                .getInRoom(roomModeRoomId, roomModeCharacterId)
                .enqueue(new retrofit2.Callback<CharacterDtos.CharacterResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<CharacterDtos.CharacterResponse> call,
                                           retrofit2.Response<CharacterDtos.CharacterResponse> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(),
                                    ApiErrors.extract(response, "Не удалось загрузить персонажа"),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        editing = CharacterMapper.fromResponse(response.body(), editing, userId);
                        fillFromCharacter(editing);
                        recalcAll();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<CharacterDtos.CharacterResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindViews(View view) {
        etCharName = view.findViewById(R.id.et_char_name);
        etLevel = view.findViewById(R.id.et_level);
        etXp = view.findViewById(R.id.et_xp);

        spinnerRace = view.findViewById(R.id.spinner_race);
        spinnerClass = view.findViewById(R.id.spinner_class);
        spinnerBackground = view.findViewById(R.id.spinner_background);
        spinnerAlignment = view.findViewById(R.id.spinner_alignment);
        spinnerHitDie = view.findViewById(R.id.spinner_hit_die);

        etAc = view.findViewById(R.id.et_ac);
        etInitBonus = view.findViewById(R.id.et_init_bonus);
        etSpeed = view.findViewById(R.id.et_speed);
        etHpMax = view.findViewById(R.id.et_hp_max);
        etHpCur = view.findViewById(R.id.et_hp_cur);
        etHpTemp = view.findViewById(R.id.et_hp_temp);
        etHitDieCurrent = view.findViewById(R.id.et_hit_die_current);

        cbInsp = view.findViewById(R.id.cb_inspiration);
        cbDeathS1 = view.findViewById(R.id.cb_death_succ_1);
        cbDeathS2 = view.findViewById(R.id.cb_death_succ_2);
        cbDeathS3 = view.findViewById(R.id.cb_death_succ_3);
        cbDeathF1 = view.findViewById(R.id.cb_death_fail_1);
        cbDeathF2 = view.findViewById(R.id.cb_death_fail_2);
        cbDeathF3 = view.findViewById(R.id.cb_death_fail_3);

        etEquipment = view.findViewById(R.id.et_equipment);
        etOtherProf = view.findViewById(R.id.et_other_proficiencies);
        etCp = view.findViewById(R.id.et_cp);
        etSp = view.findViewById(R.id.et_sp);
        etEp = view.findViewById(R.id.et_ep);
        etGp = view.findViewById(R.id.et_gp);
        etPp = view.findViewById(R.id.et_pp);

        etPersonality = view.findViewById(R.id.et_personality);
        etIdeals = view.findViewById(R.id.et_ideals);
        etBonds = view.findViewById(R.id.et_bonds);
        etFlaws = view.findViewById(R.id.et_flaws);
        etFeatures = view.findViewById(R.id.et_features);
        etBackstory = view.findViewById(R.id.et_backstory);

        etAge = view.findViewById(R.id.et_age);
        etHeight = view.findViewById(R.id.et_height);
        etWeight = view.findViewById(R.id.et_weight);
        etEyes = view.findViewById(R.id.et_eyes);
        etSkin = view.findViewById(R.id.et_skin);
        etHair = view.findViewById(R.id.et_hair);
        etAllies = view.findViewById(R.id.et_allies);
        etTreasure = view.findViewById(R.id.et_treasure);
        etMarks = view.findViewById(R.id.et_marks);
        etNotes = view.findViewById(R.id.et_notes);

        tvProfBonus = view.findViewById(R.id.tv_proficiency_bonus);
        tvInitiativeCalc = view.findViewById(R.id.tv_initiative_calc);
        tvHitDieTotal = view.findViewById(R.id.tv_hit_die_total);
        tvSpellClassInfo = view.findViewById(R.id.tv_spell_class_info);
        tvSpellDcInfo = view.findViewById(R.id.tv_spell_dc_info);
        spellsSection = view.findViewById(R.id.spells_section);

        int[] abilityIds = {R.id.ability_str, R.id.ability_dex, R.id.ability_con,
                R.id.ability_int, R.id.ability_wis, R.id.ability_cha};
        for (int i = 0; i < 6; i++) {
            View ab = view.findViewById(abilityIds[i]);
            ((TextView) ab.findViewById(R.id.tv_ability_name)).setText(ABILITY_LABELS[i]);
            abilityValueEt[i] = ab.findViewById(R.id.et_ability_value);
            abilityModTv[i] = ab.findViewById(R.id.tv_ability_mod);
        }

        buildSavingThrows(view.findViewById(R.id.saves_container));
        buildSkillRows(view.findViewById(R.id.skills_container));
        setupDeathCheckCascade(cbDeathS1, cbDeathS2, cbDeathS3);
        setupDeathCheckCascade(cbDeathF1, cbDeathF2, cbDeathF3);
    }

    private void setupDeathCheckCascade(CheckBox a, CheckBox b, CheckBox c) {
        a.setOnClickListener(v -> {
            if (!a.isChecked()) { b.setChecked(false); c.setChecked(false); }
            scheduleAutoSave();
        });
        b.setOnClickListener(v -> {
            if (b.isChecked()) { a.setChecked(true); }
            else { c.setChecked(false); }
            scheduleAutoSave();
        });
        c.setOnClickListener(v -> {
            if (c.isChecked()) { a.setChecked(true); b.setChecked(true); }
            scheduleAutoSave();
        });
    }

    private void buildSavingThrows(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        String[] saveLabels = {"Сила", "Ловкость", "Телосложение", "Интеллект", "Мудрость", "Харизма"};

        for (int i = 0; i < 6; i++) {
            View row = inflater.inflate(R.layout.item_skill_row, container, false);
            ((TextView) row.findViewById(R.id.tv_skill_name)).setText(saveLabels[i]);
            saveButtons[i] = row.findViewById(R.id.btn_proficient);
            saveValues[i] = row.findViewById(R.id.tv_skill_value);

            saveButtons[i].setTag(0);
            saveButtons[i].setText(PROF_LABELS_2[0]);
            saveButtons[i].setOnClickListener(v -> {
                int cur = (int) v.getTag();
                int next = (cur + 1) % 2;
                v.setTag(next);
                ((Button) v).setText(PROF_LABELS_2[next]);
                recalcSavesAndSkills();
            });
            container.addView(row);
        }
    }

    private void buildSkillRows(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < SKILL_NAMES.length; i++) {
            View row = inflater.inflate(R.layout.item_skill_row, container, false);
            ((TextView) row.findViewById(R.id.tv_skill_name)).setText(
                    SKILL_NAMES[i] + " (" + abilityShort(SKILL_ABILITIES[i]) + ")");
            skillButtons[i] = row.findViewById(R.id.btn_proficient);
            skillValues[i] = row.findViewById(R.id.tv_skill_value);

            skillButtons[i].setTag(0);
            skillButtons[i].setText(PROF_LABELS_3[0]);
            skillButtons[i].setOnClickListener(v -> {
                int cur = (int) v.getTag();
                int next = (cur + 1) % 3;
                v.setTag(next);
                ((Button) v).setText(PROF_LABELS_3[next]);
                recalcSavesAndSkills();
            });
            container.addView(row);
        }
    }

    private static String abilityShort(String a) {
        switch (a) {
            case "str": return "Сил";
            case "dex": return "Лов";
            case "con": return "Тел";
            case "int": return "Инт";
            case "wis": return "Мдр";
            case "cha": return "Хар";
            default: return "?";
        }
    }

    private void setupDropdowns() {
        spinnerRace.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, DndData.RACES));
        spinnerClass.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, DndData.CLASSES));
        spinnerBackground.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, DndData.BACKGROUNDS));
        spinnerAlignment.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, DndData.ALIGNMENTS));
        spinnerHitDie.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, DndData.HIT_DICE));
    }

    private void setupListeners() {
        TextWatcher recalc = simpleWatcher(this::recalcAll);
        for (EditText et : abilityValueEt) et.addTextChangedListener(recalc);
        etLevel.addTextChangedListener(recalc);
        etInitBonus.addTextChangedListener(recalc);

        spinnerClass.setOnItemClickListener((parent, v, position, id) -> {
            String cls = DndData.CLASSES[position];
            spinnerHitDie.setText(DndData.hitDieForClass(cls), false);
            recalcAll();
        });
    }

    private static TextWatcher simpleWatcher(Runnable r) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { r.run(); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void recalcAll() {
        int[] mods = new int[6];
        for (int i = 0; i < 6; i++) {
            int v = parseInt(abilityValueEt[i].getText().toString(), 10);
            mods[i] = Character.modifier(v);
            abilityModTv[i].setText(formatMod(mods[i]));
        }

        int level = clamp(parseInt(etLevel.getText().toString(), 1), 1, 20);
        int prof = Character.proficiencyBonus(level);
        tvProfBonus.setText("Бонус мастерства: " + formatMod(prof));

        int initBonus = parseInt(etInitBonus.getText().toString(), 0);
        int initiative = mods[1] + initBonus;
        tvInitiativeCalc.setText("Итоговая инициатива: " + formatMod(initiative));

        if (tvHitDieTotal != null) {
            tvHitDieTotal.setText("Костей хитов всего: " + level + " (по уровню)");
        }

        recalcSavesAndSkills();
        recalcSpellInfo();
    }

    private void recalcSavesAndSkills() {
        int[] mods = new int[6];
        for (int i = 0; i < 6; i++) {
            mods[i] = Character.modifier(parseInt(abilityValueEt[i].getText().toString(), 10));
        }
        int level = clamp(parseInt(etLevel.getText().toString(), 1), 1, 20);
        int prof = Character.proficiencyBonus(level);

        if (saveButtons[0] == null) return;
        for (int i = 0; i < 6; i++) {
            int lvl = (int) saveButtons[i].getTag();
            int v = mods[i] + (lvl == 1 ? prof : 0);
            saveValues[i].setText(formatMod(v));
        }

        for (int i = 0; i < SKILL_NAMES.length; i++) {
            int abIdx = abilityIndex(SKILL_ABILITIES[i]);
            int lvl = (int) skillButtons[i].getTag();
            int bonusFromProf = (lvl == 1) ? prof : (lvl == 2 ? prof * 2 : 0);
            int v = mods[abIdx] + bonusFromProf;
            skillValues[i].setText(formatMod(v));
        }
    }

    private static int abilityIndex(String key) {
        switch (key) {
            case "str": return 0;
            case "dex": return 1;
            case "con": return 2;
            case "int": return 3;
            case "wis": return 4;
            case "cha": return 5;
            default: return 0;
        }
    }

    private void recalcSpellInfo() {
        String cls = spinnerClass.getText() != null ? spinnerClass.getText().toString() : "";
        if (!DndData.isSpellcaster(cls)) {
            tvSpellClassInfo.setText("Класс: " + (cls.isEmpty() ? "—" : cls) + " · не является заклинателем");
            tvSpellDcInfo.setText("");
            spellsSection.setVisibility(View.GONE);
            return;
        }
        spellsSection.setVisibility(View.VISIBLE);
        String ability = DndData.spellAbilityForClass(cls);
        int level = clamp(parseInt(etLevel.getText().toString(), 1), 1, 20);
        int prof = Character.proficiencyBonus(level);
        int abMod;
        switch (ability) {
            case "Инт": abMod = Character.modifier(parseInt(abilityValueEt[3].getText().toString(), 10)); break;
            case "Мдр": abMod = Character.modifier(parseInt(abilityValueEt[4].getText().toString(), 10)); break;
            case "Хар": abMod = Character.modifier(parseInt(abilityValueEt[5].getText().toString(), 10)); break;
            default: abMod = 0;
        }
        int saveDc = 8 + prof + abMod;
        int spellAttack = prof + abMod;
        tvSpellClassInfo.setText("Класс: " + cls + " · Базовая хар-ка: " + ability);
        tvSpellDcInfo.setText(String.format(
                "Сложность спасения: %d  ·  Бонус атаки заклинанием: %s", saveDc, formatMod(spellAttack)));
    }

    private void setupAttacks(View root) {
        attacksContainer = root.findViewById(R.id.attacks_container);
        Button addBtn = root.findViewById(R.id.btn_add_attack);
        addBtn.setOnClickListener(v -> {
            if (attackRows.size() >= MAX_ATTACKS) {
                Toast.makeText(getContext(), "Максимум " + MAX_ATTACKS + " строк", Toast.LENGTH_SHORT).show();
                return;
            }
            addAttackRow(new AttackEntry());
        });
    }

    private void addAttackRow(AttackEntry data) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_attack_row, attacksContainer, false);
        EditText name = row.findViewById(R.id.et_attack_name);
        EditText bonus = row.findViewById(R.id.et_attack_bonus);
        EditText damage = row.findViewById(R.id.et_attack_damage);
        ImageButton remove = row.findViewById(R.id.btn_remove_attack);

        name.setText(data.name);
        bonus.setText(data.attackBonus == 0 && (data.name == null || data.name.isEmpty()) ? "" : String.valueOf(data.attackBonus));
        damage.setText(data.damage);

        remove.setOnClickListener(v -> {
            attacksContainer.removeView(row);
            attackRows.remove(row);
        });

        attacksContainer.addView(row);
        attackRows.add(row);
    }

    private String collectAttacksJson() {
        List<AttackEntry> list = new ArrayList<>();
        for (View row : attackRows) {
            AttackEntry e = new AttackEntry();
            e.name = textOf((EditText) row.findViewById(R.id.et_attack_name));
            if (e.name.length() > 50) e.name = e.name.substring(0, 50);
            e.attackBonus = parseInt(textOf((EditText) row.findViewById(R.id.et_attack_bonus)), 0);
            e.damage = textOf((EditText) row.findViewById(R.id.et_attack_damage));
            if (TextUtils.isEmpty(e.name) && TextUtils.isEmpty(e.damage) && e.attackBonus == 0) continue;
            list.add(e);
            if (list.size() >= MAX_ATTACKS) break;
        }
        return GSON.toJson(list);
    }

    private void loadAttacksJson(String json) {
        if (TextUtils.isEmpty(json)) return;
        try {
            Type t = new TypeToken<List<AttackEntry>>() {}.getType();
            List<AttackEntry> list = GSON.fromJson(json, t);
            if (list == null) return;
            for (AttackEntry e : list) addAttackRow(e);
        } catch (Exception ignored) {
        }
    }

    public static class AttackEntry {
        public String name = "";
        public int attackBonus = 0;
        public String damage = "";
    }

    private void setupSpells(View root) {
        spellsContainer = root.findViewById(R.id.spells_container);
        Button addBtn = root.findViewById(R.id.btn_add_spell);
        if (addBtn == null) return;
        addBtn.setOnClickListener(v -> {
            if (spellRows.size() >= MAX_SPELLS) {
                Toast.makeText(getContext(), "Максимум " + MAX_SPELLS + " заклинаний", Toast.LENGTH_SHORT).show();
                return;
            }
            addSpellRow(new SpellEntry());
        });
    }

    private void addSpellRow(SpellEntry data) {
        if (spellsContainer == null) return;
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_spell_row, spellsContainer, false);
        EditText name = row.findViewById(R.id.et_spell_name);
        EditText level = row.findViewById(R.id.et_spell_level);
        EditText school = row.findViewById(R.id.et_spell_school);
        EditText desc = row.findViewById(R.id.et_spell_desc);
        CheckBox prepared = row.findViewById(R.id.cb_spell_prepared);
        ImageButton remove = row.findViewById(R.id.btn_remove_spell);

        name.setText(data.name);
        level.setText(String.valueOf(data.level));
        school.setText(data.school);
        desc.setText(data.description);
        prepared.setChecked(data.prepared);

        remove.setOnClickListener(v -> {
            spellsContainer.removeView(row);
            spellRows.remove(row);
        });

        spellsContainer.addView(row);
        spellRows.add(row);
    }

    private String collectSpellsJson() {
        List<SpellEntry> list = new ArrayList<>();
        for (View row : spellRows) {
            SpellEntry e = new SpellEntry();
            e.name = textOf((EditText) row.findViewById(R.id.et_spell_name));
            if (e.name.length() > 80) e.name = e.name.substring(0, 80);
            e.level = clamp(parseInt(textOf((EditText) row.findViewById(R.id.et_spell_level)), 0), 0, 9);
            e.school = textOf((EditText) row.findViewById(R.id.et_spell_school));
            e.description = textOf((EditText) row.findViewById(R.id.et_spell_desc));
            e.prepared = ((CheckBox) row.findViewById(R.id.cb_spell_prepared)).isChecked();
            if (TextUtils.isEmpty(e.name) && TextUtils.isEmpty(e.description)
                    && TextUtils.isEmpty(e.school) && e.level == 0 && !e.prepared) continue;
            list.add(e);
            if (list.size() >= MAX_SPELLS) break;
        }
        return GSON.toJson(list);
    }

    private void loadSpellsJson(String json) {
        if (TextUtils.isEmpty(json)) return;
        try {
            Type t = new TypeToken<List<SpellEntry>>() {}.getType();
            List<SpellEntry> list = GSON.fromJson(json, t);
            if (list == null) return;
            for (SpellEntry e : list) addSpellRow(e);
        } catch (Exception ignored) {
        }
    }

    public static class SpellEntry {
        public String name = "";
        public int level = 0;
        public String school = "";
        public String description = "";
        public boolean prepared = false;
    }

    private void fillFromCharacter(Character c) {
        suppressWatchers = true;
        try {
            fillFromCharacterInternal(c);
        } finally {
            suppressWatchers = false;
        }
    }

    private void fillFromCharacterInternal(Character c) {
        etCharName.setText(c.characterName);
        spinnerRace.setText(c.race, false);
        spinnerClass.setText(c.characterClass, false);
        spinnerBackground.setText(c.background, false);
        spinnerAlignment.setText(c.alignment, false);
        etLevel.setText(String.valueOf(c.level));
        etXp.setText(String.valueOf(c.experiencePoints));

        int[] vals = {c.strength, c.dexterity, c.constitution, c.intelligence, c.wisdom, c.charisma};
        for (int i = 0; i < 6; i++) abilityValueEt[i].setText(String.valueOf(vals[i]));

        etAc.setText(String.valueOf(c.armorClass));
        etInitBonus.setText(String.valueOf(c.initiativeBonus));
        etSpeed.setText(String.valueOf(c.speed));
        etHpMax.setText(String.valueOf(c.maxHp));
        etHpCur.setText(String.valueOf(c.currentHp));
        etHpTemp.setText(String.valueOf(c.tempHp));
        spinnerHitDie.setText(c.hitDie, false);
        etHitDieCurrent.setText(String.valueOf(c.hitDieCurrent));

        cbInsp.setChecked(c.inspiration);
        applyDeathChecks(c.deathSaveSuccesses, cbDeathS1, cbDeathS2, cbDeathS3);
        applyDeathChecks(c.deathSaveFailures, cbDeathF1, cbDeathF2, cbDeathF3);

        int[] saveLevels = {
                clampLevel(c.savingThrowStr, 1), clampLevel(c.savingThrowDex, 1),
                clampLevel(c.savingThrowCon, 1), clampLevel(c.savingThrowInt, 1),
                clampLevel(c.savingThrowWis, 1), clampLevel(c.savingThrowCha, 1)
        };
        for (int i = 0; i < 6; i++) {
            saveButtons[i].setTag(saveLevels[i]);
            saveButtons[i].setText(PROF_LABELS_2[saveLevels[i]]);
        }

        int[] skills = {c.skillAcrobatics, c.skillAnimalHandling, c.skillArcana, c.skillAthletics,
                c.skillDeception, c.skillHistory, c.skillInsight, c.skillIntimidation,
                c.skillInvestigation, c.skillMedicine, c.skillNature, c.skillPerception,
                c.skillPerformance, c.skillPersuasion, c.skillReligion,
                c.skillSleightOfHand, c.skillStealth, c.skillSurvival};
        for (int i = 0; i < skills.length && i < skillButtons.length; i++) {
            int lvl = clampLevel(skills[i], 2);
            skillButtons[i].setTag(lvl);
            skillButtons[i].setText(PROF_LABELS_3[lvl]);
        }

        etEquipment.setText(c.equipment);
        etOtherProf.setText(c.otherProficiencies);
        etCp.setText(String.valueOf(c.copperPieces));
        etSp.setText(String.valueOf(c.silverPieces));
        etEp.setText(String.valueOf(c.electrumPieces));
        etGp.setText(String.valueOf(c.goldPieces));
        etPp.setText(String.valueOf(c.platinumPieces));

        etPersonality.setText(c.personalityTraits);
        etIdeals.setText(c.ideals);
        etBonds.setText(c.bonds);
        etFlaws.setText(c.flaws);
        etFeatures.setText(c.featuresAndTraits);
        etBackstory.setText(c.backstory);

        etAge.setText(String.valueOf(c.age));
        etHeight.setText(String.valueOf(c.height));
        etWeight.setText(String.valueOf(c.weight));
        etEyes.setText(c.eyes);
        etSkin.setText(c.skin);
        etHair.setText(c.hair);
        etAllies.setText(c.alliesAndOrganizations);
        etTreasure.setText(c.treasure);
        etMarks.setText(c.distinguishingMarks);
        etNotes.setText(c.additionalNotes);

        loadAttacksJson(c.attacksJson);
        loadSpellsJson(c.spellsJson);
    }

    private static void applyDeathChecks(int n, CheckBox a, CheckBox b, CheckBox c) {
        a.setChecked(n >= 1); b.setChecked(n >= 2); c.setChecked(n >= 3);
    }

    private static int countChecks(CheckBox a, CheckBox b, CheckBox c) {
        return (a.isChecked() ? 1 : 0) + (b.isChecked() ? 1 : 0) + (c.isChecked() ? 1 : 0);
    }

    private static int clampLevel(int v, int max) {
        if (v < 0) return 0;
        if (v > max) return max;
        return v;
    }

    private void save(boolean silent) {
        Character c = (editing != null) ? editing : new Character();
        c.userId = new SessionManager(requireContext()).getUserId();

        String name = textOf(etCharName);
        if (TextUtils.isEmpty(name) || name.length() > 50) {
            if (!silent) {
                Toast.makeText(getContext(), "Имя персонажа: 1–50 символов", Toast.LENGTH_SHORT).show();
                etCharName.setError("1–50 символов");
            }
            return;
        }
        c.characterName = name;
        String accountName = new SessionManager(requireContext()).getUsername();
        c.playerName = TextUtils.isEmpty(accountName) ? "" : accountName;

        c.race = textOf(spinnerRace);
        c.characterClass = textOf(spinnerClass);
        c.background = textOf(spinnerBackground);
        c.alignment = textOf(spinnerAlignment);

        if (TextUtils.isEmpty(c.race) || TextUtils.isEmpty(c.characterClass)) {
            if (!silent) {
                Toast.makeText(getContext(), "Выберите расу и класс", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        c.level = clamp(parseInt(textOf(etLevel), 1), 1, 20);
        c.experiencePoints = clamp(parseInt(textOf(etXp), 0), 0, 999999);

        int[] abVals = new int[6];
        for (int i = 0; i < 6; i++) {
            abVals[i] = clamp(parseInt(textOf(abilityValueEt[i]), 10), 1, 30);
        }
        c.strength = abVals[0]; c.dexterity = abVals[1]; c.constitution = abVals[2];
        c.intelligence = abVals[3]; c.wisdom = abVals[4]; c.charisma = abVals[5];

        c.armorClass = clamp(parseInt(textOf(etAc), 10), 1, 50);
        c.initiativeBonus = clamp(parseInt(textOf(etInitBonus), 0), -20, 20);
        c.speed = clamp(parseInt(textOf(etSpeed), 30), 0, 200);
        c.maxHp = clamp(parseInt(textOf(etHpMax), 1), 1, 999);
        c.currentHp = Math.min(clamp(parseInt(textOf(etHpCur), c.maxHp), 0, 999), c.maxHp);
        c.tempHp = clamp(parseInt(textOf(etHpTemp), 0), 0, 999);
        String hd = textOf(spinnerHitDie);
        c.hitDie = TextUtils.isEmpty(hd) ? DndData.hitDieForClass(c.characterClass) : hd;
        c.hitDieCurrent = clamp(parseInt(textOf(etHitDieCurrent), c.level), 0, c.level);

        c.deathSaveSuccesses = countChecks(cbDeathS1, cbDeathS2, cbDeathS3);
        c.deathSaveFailures = countChecks(cbDeathF1, cbDeathF2, cbDeathF3);
        c.inspiration = cbInsp.isChecked();

        c.savingThrowStr = (int) saveButtons[0].getTag();
        c.savingThrowDex = (int) saveButtons[1].getTag();
        c.savingThrowCon = (int) saveButtons[2].getTag();
        c.savingThrowInt = (int) saveButtons[3].getTag();
        c.savingThrowWis = (int) saveButtons[4].getTag();
        c.savingThrowCha = (int) saveButtons[5].getTag();

        int[] skillVals = new int[SKILL_NAMES.length];
        for (int i = 0; i < SKILL_NAMES.length; i++)
            skillVals[i] = (int) skillButtons[i].getTag();

        c.skillAcrobatics = skillVals[0];
        c.skillAnimalHandling = skillVals[1];
        c.skillArcana = skillVals[2];
        c.skillAthletics = skillVals[3];
        c.skillDeception = skillVals[4];
        c.skillHistory = skillVals[5];
        c.skillInsight = skillVals[6];
        c.skillIntimidation = skillVals[7];
        c.skillInvestigation = skillVals[8];
        c.skillMedicine = skillVals[9];
        c.skillNature = skillVals[10];
        c.skillPerception = skillVals[11];
        c.skillPerformance = skillVals[12];
        c.skillPersuasion = skillVals[13];
        c.skillReligion = skillVals[14];
        c.skillSleightOfHand = skillVals[15];
        c.skillStealth = skillVals[16];
        c.skillSurvival = skillVals[17];

        c.equipment = textOf(etEquipment);
        c.otherProficiencies = textOf(etOtherProf);
        c.copperPieces = clamp(parseInt(textOf(etCp), 0), 0, 999999);
        c.silverPieces = clamp(parseInt(textOf(etSp), 0), 0, 999999);
        c.electrumPieces = clamp(parseInt(textOf(etEp), 0), 0, 999999);
        c.goldPieces = clamp(parseInt(textOf(etGp), 0), 0, 999999);
        c.platinumPieces = clamp(parseInt(textOf(etPp), 0), 0, 999999);

        c.personalityTraits = textOf(etPersonality);
        c.ideals = textOf(etIdeals);
        c.bonds = textOf(etBonds);
        c.flaws = textOf(etFlaws);
        c.featuresAndTraits = textOf(etFeatures);
        c.backstory = textOf(etBackstory);

        c.age = clamp(parseInt(textOf(etAge), 0), 0, 999);
        c.height = clamp(parseInt(textOf(etHeight), 0), 0, 999);
        c.weight = clamp(parseInt(textOf(etWeight), 0), 0, 999);
        c.eyes = textOf(etEyes);
        c.skin = textOf(etSkin);
        c.hair = textOf(etHair);
        c.alliesAndOrganizations = textOf(etAllies);
        c.treasure = textOf(etTreasure);
        c.distinguishingMarks = textOf(etMarks);
        c.additionalNotes = textOf(etNotes);

        c.attacksJson = collectAttacksJson();
        c.spellsJson = collectSpellsJson();

        c.spellcastingClass = DndData.isSpellcaster(c.characterClass) ? c.characterClass : "";
        c.spellcastingAbility = DndData.spellAbilityForClass(c.characterClass);

        c.updatedAt = System.currentTimeMillis();

        SessionManager session = new SessionManager(requireContext());
        boolean useServer = session.hasServerSession();

        if (roomMode) {
            return;
        } else if (useServer) {
            saveToServer(c, session, null, false, silent);
        } else {
            saveLocal(c);
            if (!silent) {
                Toast.makeText(getContext(), "Персонаж сохранён локально", Toast.LENGTH_SHORT).show();
                safePopBackStack();
            }
        }
    }

    private void saveToServerRoomMode(Character c, SessionManager session) {
        CharacterDtos.CharacterUpsertRequest req = CharacterMapper.toUpsert(c);
        ApiClient.get(requireContext()).characters()
                .updateInRoom(roomModeRoomId, roomModeCharacterId, req)
                .enqueue(new retrofit2.Callback<CharacterDtos.CharacterResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<CharacterDtos.CharacterResponse> call,
                                           retrofit2.Response<CharacterDtos.CharacterResponse> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(),
                                    ApiErrors.extract(response, "Ошибка сохранения"),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        editing = CharacterMapper.fromResponse(response.body(), editing, session.getUserId());
                        Toast.makeText(getContext(), "Сохранено", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<CharacterDtos.CharacterResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Сеть: " + ApiErrors.fromThrowable(t, "ошибка"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void safePopBackStack() {
        try {
            Navigation.findNavController(requireView()).popBackStack();
        } catch (IllegalStateException ignored) {

        }
    }

    private void saveLocal(Character c) {
        CharacterDao dao = AppDatabase.getInstance(requireContext()).characterDao();
        if (editing == null) {
            dao.insert(c);
        } else {
            dao.update(c);
        }
    }

    private void saveToServer(Character c, SessionManager session,
                              String activeRoomId, boolean inRoom, boolean silent) {
        CharacterDtos.CharacterUpsertRequest req = CharacterMapper.toUpsert(c);
        retrofit2.Call<CharacterDtos.CharacterResponse> call;
        com.example.android.net.api.CharactersApi api =
                ApiClient.get(requireContext()).characters();

        boolean hasServerId = !TextUtils.isEmpty(c.serverCharacterId);
        if (inRoom) {
            if (hasServerId) {
                call = api.updateInRoom(activeRoomId, c.serverCharacterId, req);
            } else {
                call = api.createInRoom(activeRoomId, req);
            }
        } else {
            if (hasServerId) {
                call = api.updateTemplate(c.serverCharacterId, req);
            } else {
                call = api.createTemplate(req);
            }
        }

        call.enqueue(new retrofit2.Callback<CharacterDtos.CharacterResponse>() {
            @Override
            public void onResponse(retrofit2.Call<CharacterDtos.CharacterResponse> call,
                                   retrofit2.Response<CharacterDtos.CharacterResponse> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    String detail = ApiErrors.extract(response, "Ошибка сохранения");
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (!TextUtils.isEmpty(body)) {
                                detail = "[" + response.code() + "] " + body;
                            }
                        }
                    } catch (Exception ignored) {}
                    android.util.Log.e("CharacterEditor",
                            "save failed code=" + response.code() + " body=" + detail);
                    if (!silent) {
                        Toast.makeText(getContext(), detail, Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                Character merged = CharacterMapper.fromResponse(response.body(), c, session.getUserId());
                saveLocal(merged);
                editing = merged;
                if (!silent) {
                    Toast.makeText(getContext(), "Персонаж сохранён на сервере", Toast.LENGTH_SHORT).show();
                    safePopBackStack();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CharacterDtos.CharacterResponse> call, Throwable t) {
                if (!isAdded()) return;

                saveLocal(c);
                if (!silent) {
                    Toast.makeText(getContext(),
                            "Сохранено локально (сеть: " + ApiErrors.fromThrowable(t, "ошибка") + ")",
                            Toast.LENGTH_LONG).show();
                    safePopBackStack();
                }
            }
        });
    }

    private static String textOf(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static int parseInt(String s, int def) {
        if (TextUtils.isEmpty(s) || "-".equals(s)) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static String formatMod(int v) {
        return (v >= 0 ? "+" : "") + v;
    }
}
