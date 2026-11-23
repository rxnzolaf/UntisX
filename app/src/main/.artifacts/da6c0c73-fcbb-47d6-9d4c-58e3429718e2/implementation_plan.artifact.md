# Fix Custom Timetable Bug & Add Full Offline Management (Classes, Teachers, Rooms, Subjects, Time Grid)

## User Review Required
> [!IMPORTANT]
> 1. **Bug Fix (Problem 1):** Fix reactive timetable updating so newly added/edited custom lessons appear immediately in the day and week views and persist across app restarts (observing `getCustomLessonsFlow` in `TimetableViewModel`).
> 2. **Management Feature (Problem 2):** Add full offline management for custom profiles (Classes, Teachers, Rooms, Subjects, Time Grid) with database entities, migration (Room v14 -> v15), pickers with "Add new..." in the lesson editor, and timetable filtering support.

## Proposed Changes

### 1. Fix Problem 1 (Timetable Reactive Updates)
#### [MODIFY] [TimetableViewModel.kt](file:///C:/Users/Olaf/Desktop/UntisX/app/src/main/java/com/rxnzolaf/untis/ui/pages/timetable/TimetableViewModel.kt)
- Observe `userDao.getCustomLessonsFlow(currentUser.id)` when `currentUser.isCustom` is true, automatically triggering a reload/re-emit of timetable events whenever custom lessons are added, edited, or deleted.

### 2. Implement Problem 2 (Classes, Teachers, Rooms, Subjects, Time Grid Entities & Management)
#### [NEW] Custom Entities (`CustomClassEntity`, `CustomTeacherEntity`, `CustomRoomEntity`, `CustomSubjectEntity`, `CustomTimeGridEntity`)
- Create Room entities for custom profile metadata (all keyed by `userId`).
- Update `UserDatabase` to version 15 with auto-migration.

#### [NEW] Management UI & Lesson Editor Pickers
- Create management screens/dialogs for Classes, Teachers, Rooms, Subjects, and Time Grid.
- Update `CustomLessonEditorDialog` with dropdown pickers for Class, Subject, Teacher, and Room, including an "Add new..." option directly in the picker.
- Update `TimetableViewModel` to support custom time grids and filtering by custom class/teacher/room.

## Verification Plan

### Automated Tests
- Run unit tests: `./gradlew testGmsDebugUnitTest`

### Manual Verification
- Create custom profile, add classes, teachers, rooms, subjects, and time grid entries.
- Add a lesson using these entries and verify it appears immediately in day and week views.
- Test filtering by class/teacher/room, restarting the app, and profile isolation.
