
function buildLevel()	
	--Create player(s)
    players = {}    
    for playerId=1,8 do
        local charaId = getSelectedCharacter(playerId)
        if charaId > 0 then
            charaId = math.max(1, math.min(#charaConfigs, charaId))
            local config = charaConfigs[charaId]
            players[playerId] = config.new(playerId, config)
        end
    end

	--Create OSD
	local paramX = gameField:getX() + gameField:getWidth() + 10
	local paramY = gameField:getY() + 10 + 20
	local ww = screenWidth - paramX - 20
	
    for n=1,#players do
        paramY = createPlayerStatsDisplay(paramX, paramY, ww, 7, players[n])
        paramY = paramY + 30
    end
end

function createPlayerStatsDisplay(x, y, w, align, player)
	local dy = 15

	ParamText.new(overlayField, player, "lives", "Lives: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "bombs", "Bombs: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "shotPower", "Power: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "grazeCounter", "Graze: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "points", "Points: ", x, y, align, w)
	y = y + dy
    
    return y
end

scrollingBackgroundTiles = {}

function scrollingBackground(tex, dx, dy)
	for i,v in ipairs(scrollingBackgroundTiles) do
		v:destroy()
	end
	scrollingBackgroundTiles = {}

	local tw = tex:getWidth()
	local th = tex:getHeight()
	
	local minX = -tw/2
	local minY = -th/2
	local maxX = levelWidth + tw/2
	local maxY = levelHeight + th/2
	
	local reqImagesX = math.ceil((maxX-minX) / tw)
	local reqImagesY = math.ceil((maxY-minY) / th)
		
	for x=1,reqImagesX do
		for y=1, reqImagesY do
			local d = Drawable.new(gameField)
			d:setPos(x * tw, y * th)			
			d:setZ(32001)
			d:setTexture(tex)
			d.update = function(self)
				while true do
					local x = d:getX() + dx
					local y = d:getY() + dy

					if x > maxX then x = x - (maxX - minX) end
					if y > maxY then y = y - (maxY - minY) end
					
					d:setPos(x, y)
					yield()
				end
			end
			
			table.insert(scrollingBackgroundTiles, d)
		end
	end		
end
