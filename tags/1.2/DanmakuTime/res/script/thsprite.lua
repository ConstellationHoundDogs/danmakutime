
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THSprite = {
	hp=1,
	power=1,
	grazed=false,
    pointValue=0,
    damageFlashThread=nil,
    damageFlash=0,
    maxDamageFlash=10,
    invincible=false
	}

function THSprite.new(self)
	self = extend(THSprite, self or {})
	return Sprite.new(self)
end

function THSprite:onCollision(other, myNode, otherNode)
    if self.invincible then
        return
    end

	self.hp = self.hp - other.power
	if self.hp > 0 then
        self:onDamaged()
    else
		if self:destroy() then
			self:dropItems()
			Explosion.new(self)
		end
	end
end

function THSprite:onDamaged()
    self.damageFlash = self.maxDamageFlash

    if self.damageFlashThread == nil or self.damageFlashThread:isFinished() then
        self.damageFlashThread = self:addThread(function(self)
            local r = self:getRed()
            local g = self:getGreen()
            local b = self:getBlue()
            local a = self:getAlpha()
            while self.damageFlash > 0 do
                self.damageFlash = self.damageFlash - 1
                local f = 1.0 - (self.damageFlash / self.maxDamageFlash)
                self:setColor(f * r, f * g, b, a)
                yield()
            end
        end)
    end
end

function THSprite:dropItems()	
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THShot = {
    owner=nil
    }

function THShot.new(owner, self)
    self = extend(THShot, self or {})
    self = THSprite.new(self)
    
    self.owner = owner
    
    if owner ~= nil then
        self:setPos(owner:getX(), owner:getY())
    end
    self:setZ(500)
    
    return self
end    
    
function THShot:onCollision(other, myNode, otherNode)
    self:destroy()
end
    
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
