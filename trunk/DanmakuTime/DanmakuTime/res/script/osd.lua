
ParamText = {
	object=nil,
	param=nil,
	label=nil
	}

function ParamText.new(field, obj, paramName, lbl, x, y, blockAnchor)
	local o = extend(ParamText, {object=obj, param=paramName, label=lbl})
	o = TextDrawable.new(field, o)
	o:setPos(x or 0, y or 0)
	o:setBlockAnchor(blockAnchor or 7)	
	return o
end

function ParamText:init()
	self:setBlockAnchor(7)
	self:setFontName("DejaVuSans") --fontname is the file name without extension
	self:setFontStyle(FontStyle.BOLD)
	self:setFontSize(14)	
end

function ParamText:update()
	while true do
		local value = self.object[self.param]
		if value == nil then value = "nil" end
		
		self:setText(self.label .. ": " .. value)
		yield()
	end
end
