
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

CharaSelector = {
    playerId=1,
    options=nil,
    index=0,
    selected=false
	}

function CharaSelector.new(field, pid, self)
	self = extend(CharaSelector, self or {})
    self = Drawable.new(field, self)
    
    self.playerId = pid
    self.options = self.options or {}
    
	return self
end

function CharaSelector:update()
    while true do
        if self.index <= 0 then
            if input:consumeKey(vkeys[self.playerId].BUTTON1) then
                self:setIndex(self.playerId)
            end
        else
            if not self.selected then
                if input:consumeKey(vkeys[self.playerId].LEFT) then
                    self:setIndex(self.index - 1)
                elseif input:consumeKey(vkeys[self.playerId].RIGHT) then
                    self:setIndex(self.index + 1)
                end
                
                if input:consumeKey(vkeys[self.playerId].BUTTON1) then
                    self.selected = true
                elseif input:consumeKey(vkeys[self.playerId].BUTTON2) and self.playerId > 1 then
                    self.index = 0
                end
            else
                if input:consumeKey(vkeys[self.playerId].BUTTON2) then
                    self.selected = false
                end
            end
        end
            
        yield()
    end
end

function CharaSelector:animate()
    while true do
        self:setTexture(nil)

        local opt = self.options[self.index]
        if opt ~= nil then
            local preview = opt.preview
            if preview ~= nil then
                self:setTexture(texStore:get(preview))
            end
        end
        
        if self.selected then
            self:setColor(0.5, 0.5, 0.5, 1.0)
        else
            self:setColor(1.0, 1.0, 1.0, 1.0)
        end
    
        yield()
    end
end

function CharaSelector:setIndex(s)
    self.index = s

    while self.index <= 0 do
        self.index = self.index + #self.options
    end
    
    while self.index > #self.options do
        self.index = self.index - #self.options
    end
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

function resetSelectedCharacters()
    for i=1,8 do
        storage:remove("selected.p" .. i)
    end
    storage:set("selected.p1", 1)
end

function getSelectedCharacter(playerId)
    if playerId == nil then
        return 0
    end
    local res = storage:get("selected.p" .. playerId)
    if res == nil then
        return 0
    end
    return res
end

function charaSelectMenu(maxPlayers, numPlayers)
	setBackground("chara-select.png", 30)

    resetSelectedCharacters()
    
    numPlayers = numPlayers or 1
    maxPlayers = maxPlayers or 4
		            
    local selectors = {}
    for i=1,maxPlayers do
        selectors[i] = CharaSelector.new(0, i, {options=charaConfigs})
        if i <= numPlayers then
            selectors[i]:setIndex(storage:get("selected.p" .. i) or i)
        end
    end
	
	--Event loop
    while true do
        local allStarted = true
        local visiblePlayers = 0
        for i,v in ipairs(selectors) do            
            if v.index > 0 then
                v:setVisible(true)
                visiblePlayers = visiblePlayers + 1
                if not v.selected then
                    allStarted = false
                end
            else
                v:setVisible(false)
            end        
        end

        for i,v in ipairs(selectors) do
            v:setPos((screenWidth - visiblePlayers * 160) / 2 - 80 + 160*i, screenHeight - 160)
        end
        
        if allStarted then
            break
        end
        yield()
    end
	
    selectedPlayers = {}
    for i,v in ipairs(selectors) do
        storage:set("selected.p" .. i, v.index)
        v:destroy()
    end
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
