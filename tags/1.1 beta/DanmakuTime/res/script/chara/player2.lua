
local Player2 = {
    texture="chara/player2/sprite.png",
    preview="chara/player2/preview.png",
	speed=3,
	focusSpeed=1.5,
	fireDelay=3
    }

function Player2.new(playerId, self)
	self = extend(Player2, self or {})
	self = THPlayer.new(playerId, self)

    return self
end

table.insert(charaConfigs, Player2)
