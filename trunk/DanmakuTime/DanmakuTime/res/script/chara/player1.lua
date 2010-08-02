
local Player1 = {
    texture="chara/player1/sprite.png",
    preview="chara/player1/preview.png",
	speed=3,
	focusSpeed=1.5,
	fireDelay=3
    }

function Player1.new(playerId, self)
	self = extend(Player1, self or {})
	self = THPlayer.new(playerId, self)

    return self
end

table.insert(charaConfigs, Player1)

