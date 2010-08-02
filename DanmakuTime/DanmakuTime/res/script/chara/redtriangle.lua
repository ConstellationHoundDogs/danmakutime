
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

local RedTriangleShot = {
    power=1
    }

function RedTriangleShot.new(owner, self)
    self = extend(RedTriangleShot, self or {})
    self = THPlayerShot.new(owner, self)

	self:setColNode(0, playerShotColType, CircleColNode.new(7))
    self:setScale(.5, .5)
	self:setSpeed(16)

    return self
end

function RedTriangleShot:update()
    yield(30)
    self:setAngleInc(0)
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

local RedTriangleBomb = {
    hp=30,
    power=.5,
    parent=nil,
    rot=0,
    rotInc=8
    }

function RedTriangleBomb.new(parent, ty, self)
    self = extend(RedTriangleBomb, self or {})
    self = THSprite.new(self)

    self.parent = parent
    
    if ty == 0 then
        self:setTexture(texStore:get("chara/redtriangle/sprite.png#shotRed"))
    else
        self:setTexture(texStore:get("chara/redtriangle/sprite.png#shotBlue"))
        self.rotInc = -self.rotInc
    end
    
    self:setZ(parent:getZ() + 20)
	self:setColNode(0, playerBombColType, CircleColNode.new(7))
    self:setScale(.5, .5)

    self:setOutOfBoundsDeath(false)
    
    return self
end

function RedTriangleBomb:onCollision(other, myNode, otherNode)
    self.hp = math.max(0, self.hp - 1)
end

function RedTriangleBomb:update()
    local frame = 1
    local size = 0
    local maxSize = 1
    
    while not self.parent:isDestroyed() do
        local parent = self.parent
        if frame <= 270 and self.hp > 0 then
            size = math.min(maxSize, size * 1.01 + 0.01)
        else
            size = size * 0.90
            if size <= .01 then
                break
            end
        end
        
        self.rot = self.rot + self.rotInc
        local dx = 50 * math.sin(self.rot)
        local dy = 50 * math.cos(self.rot)
        
        self:setScale(size, size)
        
        self:setPos(parent:getX() + dx, parent:getY() + dy)
        yield()
        
        frame = frame + 1
    end
    
    self:destroy()
end
    
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

local RedTriangle = {
    texture="chara/redtriangle/sprite.png",
    preview="chara/redtriangle/preview.png",
	speed=3,
	focusSpeed=1.5,
	fireDelay=4,
    levelPadH=16,
    levelPadV=16
    
    }

function RedTriangle.new(playerId, self)
	self = extend(RedTriangle, self or {})
	self = THPlayer.new(playerId, self)
    
    self.texShotRed = texStore:get(self.texture .. "#shotRed")
    self.texShotBlue = texStore:get(self.texture .. "#shotBlue")

 	self:setColNode(2, playerItemColType, RectColNode.new(-9, -9, 18, 18))

    return self
end

function RedTriangle:fire()
    local pow = 0
    if self.shotPower >= 80 then
        pow = 2
    elseif self.shotPower >= 40 then
        pow = 1
    end
    
	for n=-pow,pow do
		local s = RedTriangleShot.new(self)
        s:setZ(s:getZ() + math.abs(n))
        
        if n == 0 then
            s:setTexture(self.texShotBlue)
            s.power = 3
        else
            s:setTexture(self.texShotRed)
        end        
        
		s:setAngle(self:getAngle() + 16 * n)		
        if self.focus then
            s:setAngleInc(n * -1.5)
        else
            s:setAngleInc(n * -0.5)
        end
	end
end

function RedTriangle:bomb(isDeathBomb)
    self.invincible = math.max(self.invincible, 300)

    local t = 0
    for n=0, 511, 32 do
        RedTriangleBomb.new(self, t % 2, {rot=n})
        t = t + 1
    end
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
    
table.insert(charaConfigs, RedTriangle)

