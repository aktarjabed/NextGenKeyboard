#!/bin/bash
# verification_script.sh

echo "Simulating Key Presses (Conceptual Verification)"
echo "------------------------------------------------"

# This script mocks the behavior of the newly implemented logic to verify the flow.

currentComposingText=""
isPasswordMode=false

function handleKeyPress() {
    key=$1
    echo "User pressed: '$key'"

    if [[ "$key" == "SPACE" ]]; then
        commitCurrentComposingText
        commitText " "
    elif [[ "$key" == "BACKSPACE" ]]; then
        if [[ -n "$currentComposingText" ]]; then
            currentComposingText=${currentComposingText%?}
            setComposingText "$currentComposingText"
        else
            echo "ACTION: deleteSurroundingText(1, 0)"
        fi
    else
        # Normal char
        currentComposingText="${currentComposingText}${key}"
        setComposingText "$currentComposingText"
    fi
}

function commitCurrentComposingText() {
    if [[ -n "$currentComposingText" ]]; then
        echo "ACTION: Autocorrect Process -> '$currentComposingText'"
        commitText "$currentComposingText"
        currentComposingText=""
        echo "ACTION: finishComposingText()"
    fi
}

function setComposingText() {
    echo "ACTION: setComposingText('$1', 1)"
}

function commitText() {
    echo "ACTION: commitText('$1', 1)"
}

# Scenario 1: Typing "Hello"
echo "[Scenario 1] Typing 'Hello'"
handleKeyPress "H"
handleKeyPress "e"
handleKeyPress "l"
handleKeyPress "l"
handleKeyPress "o"

# Scenario 2: Pressing Space
echo -e "\n[Scenario 2] Pressing Space"
handleKeyPress "SPACE"

# Scenario 3: Typing "Worl" then Backspace then "d"
echo -e "\n[Scenario 3] Correction"
handleKeyPress "W"
handleKeyPress "o"
handleKeyPress "r"
handleKeyPress "l"
handleKeyPress "BACKSPACE"
handleKeyPress "d"
handleKeyPress "SPACE"

echo -e "\n[Verification Complete]"
